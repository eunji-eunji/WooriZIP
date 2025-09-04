package com.example.demo.dto;


import com.example.demo.entity.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class ProductForm {
    private Long id;
    private String name;
    private int price;
    private Category category;
    private int stockQuantity;

    @JsonProperty("categoryId")
    private Long categoryId;

    //  하위 모델들 (각 ProductModelDto는 여러 속성값 id를 가질 수 있음)
    private List<ProductModelDto> productModelDtoList = new ArrayList<>();

    // ✅ 삭제할 모델 인덱스 리스트
    private List<Integer> deleteIndexes = new ArrayList<>();

    private List<String> imageUrls;

    // 엔티티 변환
    public Product createProduct(Category category, Users user) {
        Product product = new Product();
        product.setName(this.name);
        product.setPrice(this.price);
        product.setCategory(category); // 엔티티 기준
        product.setUser(user);
        product.setAverageRating(0.0); // 별점 평균
        product.setStockQuantity(0);  // 초기 재고 수량 설정

        // 모델 정보 추가 (productModelDtoList)
        if (this.productModelDtoList != null && !this.productModelDtoList.isEmpty()) {
            for (ProductModelDto dto : this.productModelDtoList) {
                ProductModel model = new ProductModel();
                model.setProductModelSelect(dto.getProductModelSelect());    // 모델명(자유입력)
                model.setPrStock(dto.getPrStock());  // 재고 설정
                model.setPrice(dto.getPrice());  // 가격 설정
                model.setProduct(product);  // 상품에 모델 연결
                product.addProductModel(model);  // 상품에 모델 추가
                product.setStockQuantity(product.getStockQuantity() + dto.getPrStock());  // 재고 업데이트
            }
        }

        return product;
    }


    public static ProductForm from(Product product) {
        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setName(product.getName());
        form.setPrice(product.getPrice());
        form.setCategory(product.getCategory()); // 반드시 있어야 함

        // ====== 카테고리 계층 구조 점검 로그 ======
        Category cat = product.getCategory();
        System.out.println("[ProductForm.from] 소분류: " + (cat != null ? cat.getName() : "null") +
                ", id=" + (cat != null ? cat.getId() : "null"));
        System.out.println("[ProductForm.from] 중분류: " + (cat != null && cat.getParent() != null ? cat.getParent().getName() : "null") +
                ", id=" + (cat != null && cat.getParent() != null ? cat.getParent().getId() : "null"));
        System.out.println("[ProductForm.from] 대분류: " + (cat != null && cat.getParent() != null && cat.getParent().getParent() != null ? cat.getParent().getParent().getName() : "null") +
                ", id=" + (cat != null && cat.getParent() != null && cat.getParent().getParent() != null ? cat.getParent().getParent().getId() : "null"));
        // =========================================

        // 모델 정보 매핑 (옵션)
        if (product.getProductModels() != null && !product.getProductModels().isEmpty()) {
            List<ProductModelDto> modelDtoList = product.getProductModels().stream()
                    .map(model -> {
                        ProductModelDto dto = new ProductModelDto();
                        dto.setId(model.getId()); // ✅ 꼭 필요
                        dto.setProductModelSelect(model.getProductModelSelect());
                        dto.setPrice(model.getPrice());
                        dto.setPrStock(model.getPrStock());
                        dto.setImageUrl(model.getImageUrl());
                        // 옵션별 속성값 id 세팅
                        if (model.getModelAttributes() != null && !model.getModelAttributes().isEmpty()) {
                            List<Long> attrValueIds = model.getModelAttributes().stream()
                                    .map(pma -> pma.getAttributeValue().getId())
                                    .toList();
                            dto.setAttributeValueIds(attrValueIds);
                        }
                        return dto;
                    })
                    .toList();
            form.setProductModelDtoList(modelDtoList);
        }

        // ✅ 이미지 정보 매핑
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> imageUrls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .toList();
            form.setImageUrls(imageUrls);
        }

        return form;
    }

    public boolean hasAttributeValue(Long valueId) {
        if (productModelDtoList == null || productModelDtoList.isEmpty()) {
            return false;
        }
        return productModelDtoList.stream()
                .map(model -> model.getAttributeValueIds())
                .filter(Objects::nonNull)
                .anyMatch(ids -> ids.contains(String.valueOf(valueId)));
    }
}
