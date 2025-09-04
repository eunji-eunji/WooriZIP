package com.example.demo.service;

import com.example.demo.dto.ReviewPostDto;
import com.example.demo.entity.Product;
import com.example.demo.entity.ReviewPost;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewPostRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.constant.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.example.demo.entity.Users;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewPostService {

    private final ReviewPostRepository reviewPostRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";


    // 리뷰 조회
    public ReviewPost getReviewById(Long id) {
        return reviewPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
    }

    // 리뷰 등록
    public Long saveReview(ReviewPostDto dto) throws IOException {
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

        ReviewPost post = ReviewPost.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .rating(dto.getRating())
                .fileNames(joinedNames)
                .filePaths(joinedPaths)
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .product(product)
                .build();

        ReviewPost saved = reviewPostRepository.save(post);
        updateProductAverageRating(product.getId());
        return saved.getId();
    }

    // 리뷰 수정
    public void updateReview(Long id, ReviewPostDto dto, List<String> deleteImages) throws IOException {
        ReviewPost post = reviewPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        // 기존 이미지 경로와 파일명 처리
        List<String> currentPaths = new ArrayList<>();
        List<String> currentNames = new ArrayList<>();

        if (post.getFilePaths() != null && !post.getFilePaths().isEmpty()) {
            currentPaths.addAll(Arrays.asList(post.getFilePaths().split(",")));
            currentNames.addAll(Arrays.asList(post.getFileNames().split(",")));
        }

        // 삭제할 이미지 처리
        if (deleteImages != null && !deleteImages.isEmpty()) {
            for (String pathToDelete : deleteImages) {
                int index = currentPaths.indexOf(pathToDelete);
                if (index != -1) {
                    deleteFile(pathToDelete);  // 실제 파일 삭제
                    currentPaths.remove(index);
                    if (index < currentNames.size()) {
                        currentNames.remove(index); 
                    }
                }
            }
        }

        // 새로운 파일이 있는 경우 추가
        if (dto.getFiles() != null && !dto.getFiles().isEmpty() && !dto.getFiles().get(0).isEmpty()) {
            List<String> newPaths = handleMultipleFiles(dto.getFiles());
            currentPaths.addAll(newPaths);

            List<String> newNames = dto.getFiles().stream()
                    .map(MultipartFile::getOriginalFilename)
                    .collect(Collectors.toList());
            currentNames.addAll(newNames);
        }

        // 최종 경로와 파일명을 문자열로 변환
        String joinedPaths = String.join(",", currentPaths);
        String joinedNames = String.join(",", currentNames);

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setRating(dto.getRating());
        post.setFilePaths(joinedPaths);
        post.setFileNames(joinedNames);
        post.setUpdatedAt(LocalDateTime.now());

        reviewPostRepository.save(post);
        updateProductAverageRating(post.getProduct().getId());
    }

    // 리뷰 삭제
    public void deleteReview(Long id) {
        ReviewPost post = reviewPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
        deleteFiles(post.getFilePaths());
        reviewPostRepository.delete(post);
        updateProductAverageRating(post.getProduct().getId());
    }

    // 파일 저장
    private List<String> handleMultipleFiles(List<MultipartFile> files) throws IOException {
        List<String> filePathList = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
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

    // 단일 파일 삭제
    private void deleteFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(System.getProperty("user.dir") + filePath);
            if (file.exists()) file.delete();
        }
    }

    // 평균 평점
    public double getAverageRating(Long productId) {
        List<ReviewPost> list = reviewPostRepository.findByProductId(productId);
        return list.isEmpty() ? 0.0 : list.stream().mapToInt(ReviewPost::getRating).average().orElse(0.0);
    }

    // 별점 분포 (1~5점 카운트)
    public Map<Integer, Long> getRatingDistribution(Long productId) {
        List<ReviewPost> list = reviewPostRepository.findByProductId(productId);
        return list.stream().collect(Collectors.groupingBy(ReviewPost::getRating, Collectors.counting()));
    }

    // 최신순 / 평점순 정렬 + 페이지네이션
    public Page<ReviewPost> getReviews(Long productId, int page, String sortBy) {
        Sort sort = sortBy.equals("rating") ? Sort.by(Sort.Direction.DESC, "rating") : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, 5, sort);
        return reviewPostRepository.findByProductId(productId, pageable);
    }

    // 1인 1리뷰 제한 확인
    public boolean hasWrittenReview(Long productId, String email) {
        return reviewPostRepository.existsByProductIdAndEmail(productId, email);
    }

    // 사용자가 해당 상품을 구매했는지 확인
    public boolean hasPurchasedProduct(Long productId, String email) {
        return orderRepository.existsByUsersEmailAndOrderStatusAndOrderItemsProductId(email, OrderStatus.ORDER, productId);
    }

    // 사용자가 작성한 리뷰 목록 조회
    public List<ReviewPostDto> findByUser(Users user) {
        return reviewPostRepository.findByEmailOrderByCreatedAtDesc(user.getEmail())
                .stream()
                .filter(review -> {
                    try {
                        return review.getProduct() != null && !review.getProduct().getIsDeleted();
                    } catch (Exception e) {
                        // Product가 완전히 삭제된 경우 false 반환
                        return false;
                    }
                })
                .map(review -> {
                    try {
                        return ReviewPostDto.fromEntity(review);
                    } catch (Exception e) {
                        // Product가 완전히 삭제된 경우 기본 정보만으로 DTO 생성
                        ReviewPostDto dto = new ReviewPostDto();
                        dto.setId(review.getId());
                        dto.setTitle(review.getTitle() != null ? review.getTitle() : "제목 없음");
                        dto.setContent(review.getContent() != null ? review.getContent() : "내용 없음");
                        dto.setEmail(review.getEmail());
                        dto.setNickname(review.getNickname());
                        dto.setProductId(null);
                        dto.setRating(review.getRating());
                        dto.setCreatedAt(review.getCreatedAt());
                        dto.setUpdatedAt(review.getUpdatedAt());
                        return dto;
                    }
                })
                .collect(Collectors.toList());
    }

    // 전체 리뷰 최신순 조회
    public List<ReviewPostDto> findAllReviews() {
        return reviewPostRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(ReviewPostDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 최신 리뷰 목록 조회
    public Page<ReviewPost> findLatestReviews(Pageable pageable) {
        return reviewPostRepository.findAll(pageable);
    }

    // 상품 평균 평점 업데이트
    @Transactional
    public void updateProductAverageRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        
        List<ReviewPost> reviews = reviewPostRepository.findByProductId(productId);
        double averageRating = reviews.isEmpty() ? 0.0 : 
            reviews.stream().mapToInt(ReviewPost::getRating).average().orElse(0.0);
        
        product.setAverageRating(averageRating);
        productRepository.save(product);
    }
}