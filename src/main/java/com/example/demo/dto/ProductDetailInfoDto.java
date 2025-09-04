package com.example.demo.dto;

import com.example.demo.entity.ProductDetail;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailInfoDto {
    private Long id;
    private Long productId;
    private String productBaseName; // 기본 상품명 (편의용)
    
    // 상품 상세정보
    private String detailInfo;
    
    // 파일 첨부 관련 (인테리어 게시판과 동일한 구조)
    private List<MultipartFile> files;
    private String detailImageNames;
    private String detailImagePaths;
    private List<String> detailImagePathList;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환
    public static ProductDetailInfoDto fromEntity(ProductDetail productDetail) {
        if (productDetail == null) return null;
        
        List<String> imagePathList = null;
        if (productDetail.getDetailImagePaths() != null && !productDetail.getDetailImagePaths().isEmpty()) {
            imagePathList = Arrays.stream(productDetail.getDetailImagePaths().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        return ProductDetailInfoDto.builder()
                .id(productDetail.getId())
                .productId(productDetail.getProduct().getId())
                .productBaseName(productDetail.getProduct().getName())
                .detailInfo(productDetail.getDetailInfo())
                .detailImageNames(productDetail.getDetailImageNames())
                .detailImagePaths(productDetail.getDetailImagePaths())
                .detailImagePathList(imagePathList)
                .createdAt(productDetail.getCreatedAt())
                .updatedAt(productDetail.getUpdatedAt())
                .build();
    }
} 