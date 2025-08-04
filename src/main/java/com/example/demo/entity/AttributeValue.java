package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상품 속성의 실제 값(옵션값, ex: 화이트, 원목, 퀸 등)을 정의하는 엔티티
 * ex) 색상-화이트, 색상-블랙, 소재-원목, 사이즈-퀸 등
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 속성(Attribute)에 속하는 값인지 (ex: 색상, 소재 등)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;

    /**
     * 속성값 (ex: 화이트, 원목, 퀸 등)
     */
    @Column(nullable = false)
    private String value; // ex: 화이트, 원목, 슈퍼싱글 등

    public AttributeValue(Attribute attribute, String value) {
        this.attribute = attribute;
        this.value = value;
    }
}