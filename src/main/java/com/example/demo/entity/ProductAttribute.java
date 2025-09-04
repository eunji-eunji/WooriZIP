package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상품이 어떤 속성값(옵션값)을 가지는지 매핑하는 엔티티 (N:M)
 * ex) 침대A 상품은 색상-화이트, 사이즈-퀸을 가짐
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ProductAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 상품(Product)에 대한 속성값인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /**
     * 어떤 속성값(AttributeValue, ex: 색상-화이트, 사이즈-퀸 등)인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_value_id")
    private AttributeValue attributeValue;
}