package com.example.demo.dto;

import com.example.demo.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponseDto {
    private String message;
    private String type; // "text", "product_list", "category", "link"
    private List<Product> products;
    private String link;
    private List<String> suggestions;
} 