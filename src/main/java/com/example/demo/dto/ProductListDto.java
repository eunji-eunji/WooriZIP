package com.example.demo.dto;

import com.example.demo.entity.Product;
import com.example.demo.entity.ProductModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductListDto {

    private Long id;
    private String name;
    private int price;
    private String thumbnailUrl;
    private double averageRating;
    private int reviewCount;
    private int totalStock;        // 옵션별 재고 합계
    private String categoryName;   // 카테고리 이름 (null-safe)

    // ✅ 직접 계산 생성자
    public ProductListDto(Product product, double averageRating, int reviewCount) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.thumbnailUrl = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getImageUrl()
                : null;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;

        // 옵션 재고 총합
        this.totalStock = product.getProductModels().stream()
                .filter(pm -> pm.getPrStock() != null)
                .mapToInt(ProductModel::getPrStock)
                .sum();

        // null-safe 카테고리명
        this.categoryName = (product.getCategory() != null)
                ? product.getCategory().getName()
                : "카테고리 없음";
    }
}
