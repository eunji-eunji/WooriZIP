package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ProductModel ↔ AttributeValue N:M 연결 테이블
 * (각 옵션별로 여러 속성값을 가질 수 있도록 함)
 * 예) seat-0002(옵션) - 색상:화이트, 사이즈:L, 소재:원목 등
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ProductModelAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 옵션(ProductModel)에 대한 속성값인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_model_id")
    private ProductModel productModel;

    /**
     * 어떤 속성값(AttributeValue, ex: 색상-화이트, 사이즈-L 등)인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_value_id")
    private AttributeValue attributeValue;
}