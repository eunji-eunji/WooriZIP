package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ProductModelDto {
    private Long id;
    private String productModelSelect; // 옵션명(자유입력)
    private Integer price;                         // 옵션 가격
    private Integer prStock;                        // 기종별 재고 수량
    private String imageUrl;                        // ✅ 모델별 이미지 URL
    private String modelName; // 프론트에서 넘어오는 옵션명(임시)

    /**
     * 이 옵션(ProductModel)이 가지는 속성값 id 리스트 (ex: 색상-화이트, 사이즈-L 등)
     * ProductModelAttribute로 연결됨
     */
    private java.util.List<Long> attributeValueIds = new java.util.ArrayList<>();

    /**
     * 옵션별 속성값 id 리스트를 쉼표구분 문자열로 내려주는 필드 (템플릿 오류 방지)
     */
    private String attributeValueIdsStr = "";

    public void setModelName(String modelName) {
        this.modelName = modelName;
        this.productModelSelect = modelName;
    }
}
