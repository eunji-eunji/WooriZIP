package com.example.demo.service;

import com.example.demo.dto.QnaAnswerDto;
import com.example.demo.dto.QnaPostDto;
import com.example.demo.entity.Product;
import com.example.demo.entity.QnaPost;
import com.example.demo.entity.Users;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.QnaAnswerRepository;
import com.example.demo.repository.QnaPostRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.constant.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaPostService {

    private final QnaPostRepository qnaPostRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    // Q 등록
    @Transactional
    public Long saveQna(QnaPostDto dto) throws IOException {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        List<String> storedPaths = new ArrayList<>();
        String joinedPaths = "";
        String joinedNames = "";

        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            storedPaths = handleMultipleFiles(dto.getFiles());
            joinedPaths = String.join(",", storedPaths);
            joinedNames = dto.getFiles().stream()
                    .map(MultipartFile::getOriginalFilename)
                    .collect(Collectors.joining(","));
        }

        QnaPost post = QnaPost.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .fileNames(joinedNames)
                .filePaths(joinedPaths)
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .isSecret(dto.isSecret())
                .product(product)
                .build();

        QnaPost saved = qnaPostRepository.save(post);
        return saved.getId();
    }

    // Q 수정
    @Transactional
    public void updateQna(Long id, QnaPostDto dto) throws IOException {
        QnaPost post = qnaPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("질문이 존재하지 않습니다."));

        if (!post.getEmail().equals(dto.getEmail())) {
            throw new SecurityException("작성자만 수정할 수 있습니다.");
        }

        // 기존 이미지 경로들 가져오기
        List<String> existingPaths = new ArrayList<>();
        if (post.getFilePaths() != null && !post.getFilePaths().isEmpty()) {
            existingPaths = Arrays.asList(post.getFilePaths().split(","));
        }

        // 삭제할 이미지들 처리
        List<String> deleteImages = dto.getDeleteImages() != null ? 
            Arrays.asList(dto.getDeleteImages().split(",")) : new ArrayList<>();
        
        // 삭제할 이미지들을 기존 경로에서 제거
        existingPaths = existingPaths.stream()
                .filter(path -> !deleteImages.contains(path.trim()))
                .collect(Collectors.toList());

        // 새로 업로드된 이미지들 처리
        List<String> newPaths = new ArrayList<>();
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            newPaths = handleMultipleFiles(dto.getFiles());
        }

        // 기존 이미지 + 새 이미지 합치기
        List<String> allPaths = new ArrayList<>(existingPaths);
        allPaths.addAll(newPaths);

        // 삭제된 이미지 파일들 실제로 삭제
        for (String deletePath : deleteImages) {
            if (deletePath != null && !deletePath.trim().isEmpty()) {
                File file = new File(System.getProperty("user.dir") + deletePath.trim());
                if (file.exists()) {
                    file.delete();
                }
            }
        }

        String joinedPaths = String.join(",", allPaths);
        String joinedNames = "";
        
        // 기존 파일명들 유지 + 새 파일명들 추가
        List<String> existingNames = new ArrayList<>();
        if (post.getFileNames() != null && !post.getFileNames().isEmpty()) {
            existingNames = Arrays.asList(post.getFileNames().split(","));
        }
        
        // 삭제된 이미지에 해당하는 파일명들 제거
        existingNames = existingNames.stream()
                .filter(name -> !deleteImages.contains(name.trim()))
                .collect(Collectors.toList());

        // 새 파일명들 추가
        List<String> newNames = new ArrayList<>();
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            newNames = dto.getFiles().stream()
                    .map(MultipartFile::getOriginalFilename)
                    .collect(Collectors.toList());
        }

        List<String> allNames = new ArrayList<>(existingNames);
        allNames.addAll(newNames);
        joinedNames = String.join(",", allNames);

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setFilePaths(joinedPaths);
        post.setFileNames(joinedNames);
        post.setSecret(dto.isSecret());
        post.setUpdatedAt(LocalDateTime.now());

        qnaPostRepository.save(post);
    }

    // Q 삭제
    @Transactional
    public void deleteQna(Long id, String email) {
        QnaPost post = qnaPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("질문이 존재하지 않습니다."));

        if (!post.getEmail().equals(email)) {
            throw new SecurityException("작성자만 삭제할 수 있습니다.");
        }

        deleteFiles(post.getFilePaths());
        qnaPostRepository.delete(post);
    }

    // 파일 저장
    private List<String> handleMultipleFiles(List<MultipartFile> files) throws IOException {
        List<String> filePathList = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    File dest = new File(uploadDir + fileName);
                    file.transferTo(dest);
                    filePathList.add("/uploads/" + fileName);
                }
            }
        }
        return filePathList;
    }

    // 파일 삭제
    private void deleteFiles(String filePaths) {
        if (filePaths != null && !filePaths.isEmpty()) {
            String[] paths = filePaths.split(",");
            for (String path : paths) {
                File file = new File(System.getProperty("user.dir") + path);
                if (file.exists()) file.delete();
            }
        }
    }

    // Q 단건 조회
    public QnaPost findById(Long id) {
        return qnaPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("질문 없음"));
    }

    // 상품 정보 조회
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    // QnA DTO 리스트 (답변 포함)
    public List<QnaPostDto> getQnaPostDtoList(Long productId, int page, String filter) {
        int offset = page * 5;
        List<QnaPost> posts = qnaPostRepository.findByProductIdWithPaging(productId, offset, 5);
        if (posts == null) {
            posts = new ArrayList<>();
        }

        List<QnaPostDto> result = new ArrayList<>();

        for (QnaPost post : posts) {
            QnaPostDto dto = QnaPostDto.fromEntity(post);
            qnaAnswerRepository.findByQnaPost(post).ifPresent(answer ->
                    dto.setAnswer(QnaAnswerDto.fromEntity(answer))
            );

            // 필터 적용
            boolean shouldAdd = switch (filter) {
                case "answered" -> dto.getAnswer() != null;
                case "unanswered" -> dto.getAnswer() == null;
                default -> true; // "all" 또는 기타 값
            };

            if (shouldAdd) {
                result.add(dto);
            }
        }

        return result;
    }

    // 총 질문 수 (필터 적용)
    public long countByProduct(Long productId, String filter) {
        List<QnaPost> allPosts = qnaPostRepository.findByProductIdOrderByCreatedAtDesc(productId);

        return switch (filter) {
            case "answered" -> allPosts.stream().filter(post -> post.getAnswer() != null).count();
            case "unanswered" -> allPosts.stream().filter(post -> post.getAnswer() == null).count();
            default -> allPosts.size(); // "all" 또는 기타 값
        };
    }

    // QnA 답변 후 상품상세로 리다이렉트용
    public Long getProductIdByQnaPostId(Long postId) {
        return qnaPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("QnA 게시글이 존재하지 않습니다."))
                .getProduct().getId();
    }

    public Map<String, Long> getQnaStatistics() {
        List<QnaPost> allQna = qnaPostRepository.findAll();

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) allQna.size());
        stats.put("answered", allQna.stream().filter(qna -> qna.getAnswer() != null).count());
        stats.put("unanswered", allQna.stream().filter(qna -> qna.getAnswer() == null).count());

        return stats;
    }

        public Map<String, Map<String, Object>> getQnaByCategoryWithStatus() {
        List<QnaPost> allQna = qnaPostRepository.findAll();
        Map<String, Map<String, Object>> categoryStats = new HashMap<>();

        // 각 상품별로 QnA 게시글을 그룹화하고 생성일 기준 내림차순 정렬
        Map<Long, List<QnaPost>> productQnas = allQna.stream()
                .filter(qna -> qna.getProduct() != null) // Product가 null이 아닌 것만 필터링
                .collect(Collectors.groupingBy(
                        qna -> qna.getProduct().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                                    return list;
                                }
                            )
                        ));

        allQna.forEach(qna -> {
            try {
                // Product가 null인 경우 건너뛰기
                if (qna.getProduct() == null) {
                    return;
                }
                
                String category = qna.getProduct().getCategory().getName();
                categoryStats.putIfAbsent(category, new HashMap<>());

                Map<String, Object> stats = categoryStats.get(category);

                List<Map<String, Object>> answeredList = (List<Map<String, Object>>) stats.getOrDefault("answeredList", new ArrayList<Map<String, Object>>());
                List<Map<String, Object>> unansweredList = (List<Map<String, Object>>) stats.getOrDefault("unansweredList", new ArrayList<Map<String, Object>>());

                // 해당 상품의 QnA 목록에서 현재 게시글의 위치를 찾아 페이지 번호 계산
                List<QnaPost> productQnaList = productQnas.get(qna.getProduct().getId());
                if (productQnaList == null) {
                    return; // 해당 상품의 QnA 목록이 없으면 건너뛰기
                }
                
                int position = 0;
                for (int i = 0; i < productQnaList.size(); i++) {
                    if (productQnaList.get(i).getId().equals(qna.getId())) {
                        position = i;
                        break;
                    }
                }

                // 페이지 번호 계산 (5개씩 페이징, 최신글이 1페이지)
                int pageNum = (position / 5) + 1;

                Map<String, Object> postInfo = new HashMap<>();
                postInfo.put("post", qna);
                postInfo.put("page", pageNum);

                if (qna.getAnswer() != null) {
                    answeredList.add(postInfo);
                } else {
                    unansweredList.add(postInfo);
                }

                // 답변 완료/미답변 목록도 생성일 기준 내림차순 정렬
                Comparator<Map<String, Object>> byCreatedAt = (m1, m2) -> {
                    QnaPost p1 = (QnaPost) m1.get("post");
                    QnaPost p2 = (QnaPost) m2.get("post");
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                };

                answeredList.sort(byCreatedAt);
                unansweredList.sort(byCreatedAt);

                stats.put("answeredList", answeredList);
                stats.put("unansweredList", unansweredList);
                stats.put("answered", answeredList.size());
                stats.put("unanswered", unansweredList.size());
            } catch (Exception e) {
                // 개별 QnA 처리 중 오류가 발생해도 다른 QnA는 계속 처리
                // 오류가 발생한 QnA는 건너뛰기
            }
        });

        return categoryStats;
    }

    public List<QnaPost> getUnansweredQna() {
        return qnaPostRepository.findAll().stream()
                .filter(qna -> qna.getAnswer() == null)
                .sorted(Comparator.comparing(QnaPost::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    //사용자가 작성한 QnA 목록 조회
    public List<QnaPostDto> findByUser(Users user) {
        return qnaPostRepository.findByEmailOrderByCreatedAtDesc(user.getEmail())
                .stream()
                .filter(post -> {
                    try {
                        return post.getProduct() != null && !post.getProduct().getIsDeleted();
                    } catch (Exception e) {
                        // Product가 완전히 삭제된 경우 false 반환
                        return false;
                    }
                })
                .map(post -> {
                    try {
                        QnaPostDto dto = QnaPostDto.fromEntity(post);
                        qnaAnswerRepository.findByQnaPost(post)
                                .ifPresent(answer -> dto.setAnswer(QnaAnswerDto.fromEntity(answer)));
                        return dto;
                    } catch (Exception e) {
                        // Product가 완전히 삭제된 경우 기본 정보만으로 DTO 생성
                        QnaPostDto dto = new QnaPostDto();
                        dto.setId(post.getId());
                        dto.setTitle(post.getTitle() != null ? post.getTitle() : "제목 없음");
                        dto.setContent(post.getContent() != null ? post.getContent() : "내용 없음");
                        dto.setEmail(post.getEmail());
                        dto.setNickname(post.getNickname());
                        dto.setProductId(null);
                        dto.setCreatedAt(post.getCreatedAt());
                        dto.setUpdatedAt(post.getUpdatedAt());
                        dto.setSecret(post.isSecret());
                        return dto;
                    }
                })
                .collect(Collectors.toList());
    }

    // 사용자가 해당 상품을 구매했는지 확인
    public boolean hasPurchasedProduct(Long productId, String email) {
        return orderRepository.existsByUsersEmailAndOrderStatusAndOrderItemsProductId(email, OrderStatus.ORDER, productId);
    }

    // 관리자 대시보드에서 QnA 목록 조회
    public List<com.example.demo.dto.QnaPostDto> getAllQnaForAdminDashboard() {
        List<QnaPost> allQna = qnaPostRepository.findAll();
        List<com.example.demo.dto.QnaPostDto> result = new java.util.ArrayList<>();
        for (QnaPost post : allQna) {
            try {
                com.example.demo.dto.QnaPostDto dto = com.example.demo.dto.QnaPostDto.fromEntity(post);
                dto.setAnswered(qnaAnswerRepository.existsByQnaPost(post));
                
                // Product가 존재하는지 확인
                if (post.getProduct() != null) {
                    dto.setProductId(post.getProduct().getId());
                    dto.setProductName(post.getProduct().getName());
                } else {
                    // Product가 삭제된 경우
                    dto.setProductId(null);
                    dto.setProductName("삭제된 상품");
                }
                result.add(dto);
            } catch (Exception e) {
                // 개별 QnA 처리 중 오류가 발생해도 다른 QnA는 계속 처리
                // 오류가 발생한 QnA도 기본 정보만이라도 추가
                try {
                    com.example.demo.dto.QnaPostDto dto = new com.example.demo.dto.QnaPostDto();
                    dto.setId(post.getId());
                    dto.setTitle(post.getTitle() != null ? post.getTitle() : "제목 없음");
                    dto.setContent(post.getContent() != null ? post.getContent() : "내용 없음");
                    dto.setEmail(post.getEmail());
                    dto.setNickname(post.getNickname());
                    dto.setProductId(null);
                    dto.setProductName("삭제된 상품");
                    dto.setAnswered(qnaAnswerRepository.existsByQnaPost(post));
                    result.add(dto);
                } catch (Exception ex) {
                    // 기본 정보 생성 중에도 오류가 발생하면 해당 QnA는 건너뛰기
                }
            }
        }
        return result;
    }
}
