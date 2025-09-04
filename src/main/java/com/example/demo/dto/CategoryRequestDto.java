package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 요청용 DTO (카테고리 등록 시 사용)
@Getter
@Setter
@NoArgsConstructor
public class CategoryRequestDto {
    private String name;
    private Long parentId; // 대분류면 null
}