package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryMonthSalesDto {
    private String month;      // ex. "2025-08"
    private String category;   // ex. "침대"
    private int count;         // ex. 42
}