package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상품의 속성(옵션 종류, ex: 색상, 소재, 사이즈 등)을 정의하는 엔티티
 * ex) 색상, 소재, 사이즈, 브랜드 등
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 속성명 (ex: 색상, 소재, 사이즈 등)
     */
    @Column(nullable = false)
    private String name; // ex: 색상, 소재, 사이즈 등

    /**
     * 속성 타입 (ex: select, text 등. 확장용)
     */
    private String type;

    public Attribute(String name) {
        this.name = name;
    }
}