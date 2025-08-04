package com.example.demo.service;

import com.example.demo.dto.ProductDetailInfoDto;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductDetail;
import com.example.demo.repository.ProductDetailRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDetailService {

    private final ProductDetailRepository productDetailRepository;
    private final ProductRepository productRepository;
    
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    // ProductDetail 조회 (productId로)
    public ProductDetailInfoDto findByProductId(Long productId) {
        Optional<ProductDetail> productDetail = productDetailRepository.findByProductId(productId);
        return productDetail.map(ProductDetailInfoDto::fromEntity).orElse(null);
    }

    // ProductDetail 저장/수정
    @Transactional
    public void saveProductDetail(ProductDetailInfoDto dto, List<String> removedImages) throws IOException {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        ProductDetail productDetail;
        if (dto.getId() != null) {
            // 기존 데이터 수정
            productDetail = productDetailRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("상품 상세정보를 찾을 수 없습니다."));
        } else {
            // 새로운 데이터 생성
            Optional<ProductDetail> existing = productDetailRepository.findByProductId(dto.getProductId());
            if (existing.isPresent()) {
                productDetail = existing.get();
            } else {
                productDetail = new ProductDetail();
                productDetail.setProduct(product);
            }
        }

        // 개별 필드들 설정
        productDetail.setDetailInfo(dto.getDetailInfo());

        // 기존 이미지 삭제 처리
        if (removedImages != null && !removedImages.isEmpty()) {
            String currentPaths = productDetail.getDetailImagePaths();
            String currentNames = productDetail.getDetailImageNames();
            
            if (currentPaths != null && !currentPaths.isEmpty()) {
                List<String> pathList = new ArrayList<>(Arrays.asList(currentPaths.split(",")));
                List<String> nameList = new ArrayList<>(Arrays.asList(currentNames.split(",")));
                
                for (String removedPath : removedImages) {
                    int index = pathList.indexOf(removedPath.trim());
                    if (index != -1) {
                        pathList.remove(index);
                        if (index < nameList.size()) {
                            nameList.remove(index);
                        }
                        
                        // 실제 파일 삭제
                        try {
                            String fullPath = uploadDir + "/" + removedPath.substring(removedPath.lastIndexOf("/") + 1);
                            Files.deleteIfExists(Paths.get(fullPath));
                        } catch (IOException e) {
                            System.err.println("파일 삭제 실패: " + removedPath);
                        }
                    }
                }
                
                productDetail.setDetailImagePaths(pathList.isEmpty() ? null : String.join(",", pathList));
                productDetail.setDetailImageNames(nameList.isEmpty() ? null : String.join(",", nameList));
            }
        }

        // 새 이미지 설정 (컨트롤러에서 이미 처리됨)
        if (dto.getDetailImagePaths() != null && !dto.getDetailImagePaths().isEmpty()) {
            // 기존 이미지와 새 이미지 합치기
            List<String> imagePaths = new ArrayList<>();
            List<String> imageNames = new ArrayList<>();
            
            // 기존 이미지가 있다면 추가
            if (productDetail.getDetailImagePaths() != null && !productDetail.getDetailImagePaths().isEmpty()) {
                imagePaths.addAll(Arrays.asList(productDetail.getDetailImagePaths().split(",")));
            }
            if (productDetail.getDetailImageNames() != null && !productDetail.getDetailImageNames().isEmpty()) {
                imageNames.addAll(Arrays.asList(productDetail.getDetailImageNames().split(",")));
            }
            
            // 새 이미지 추가
            imagePaths.addAll(Arrays.asList(dto.getDetailImagePaths().split(",")));
            imageNames.addAll(Arrays.asList(dto.getDetailImageNames().split(",")));
            
            productDetail.setDetailImagePaths(String.join(",", imagePaths));
            productDetail.setDetailImageNames(String.join(",", imageNames));
        }

        productDetailRepository.save(productDetail);
    }

    // ProductDetail 존재 여부 확인
    public boolean existsByProductId(Long productId) {
        return productDetailRepository.existsByProductId(productId);
    }

    // 파일 업로드 처리 (인테리어 게시판과 동일한 로직)
    private List<String> handleMultipleFiles(List<MultipartFile> files) throws IOException {
        List<String> filePaths = new ArrayList<>();
        
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalName = file.getOriginalFilename();
                if (originalName != null) {
                    String uniqueName = System.currentTimeMillis() + "_" + originalName;
                    File dest = new File(uploadDir + uniqueName);
                    file.transferTo(dest);
                    filePaths.add("/uploads/" + uniqueName);
                }
            }
        }

        return filePaths;
    }

    // 기존 이미지 삭제 처리
    private void handleRemovedImages(ProductDetail productDetail, List<String> removedImages) {
        if (productDetail.getDetailImagePaths() == null || productDetail.getDetailImagePaths().isEmpty()) {
            return;
        }

        List<String> existingPaths = new ArrayList<>(
                Arrays.asList(productDetail.getDetailImagePaths().split(","))
        );
        List<String> existingNames = new ArrayList<>();
        
        if (productDetail.getDetailImageNames() != null && !productDetail.getDetailImageNames().isEmpty()) {
            existingNames.addAll(Arrays.asList(productDetail.getDetailImageNames().split(",")));
        }

        // 삭제할 이미지들 처리
        for (String removedImagePath : removedImages) {
            int index = existingPaths.indexOf(removedImagePath.trim());
            if (index >= 0) {
                // 실제 파일 삭제
                File file = new File(System.getProperty("user.dir") + removedImagePath.trim());
                if (file.exists()) {
                    file.delete();
                }
                
                // 리스트에서 제거
                existingPaths.remove(index);
                if (index < existingNames.size()) {
                    existingNames.remove(index);
                }
            }
        }

        // 업데이트된 이미지 정보 저장
        productDetail.setDetailImagePaths(String.join(",", existingPaths));
        productDetail.setDetailImageNames(String.join(",", existingNames));
    }

    // ProductDetail 삭제
    @Transactional
    public void deleteByProductId(Long productId) {
        Optional<ProductDetail> productDetail = productDetailRepository.findByProductId(productId);
        if (productDetail.isPresent()) {
            // 이미지 파일들 삭제
            ProductDetail detail = productDetail.get();
            if (detail.getDetailImagePaths() != null && !detail.getDetailImagePaths().isEmpty()) {
                String[] imagePaths = detail.getDetailImagePaths().split(",");
                for (String path : imagePaths) {
                    File file = new File(System.getProperty("user.dir") + path.trim());
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
            productDetailRepository.delete(detail);
        }
    }
} 