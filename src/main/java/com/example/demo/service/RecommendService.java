package com.example.demo.service;

import com.example.demo.entity.Product;
import com.example.demo.entity.ProductModelAttribute;
import com.example.demo.repository.ProductModelAttributeRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ProductRepository productRepository;
    private final ProductModelAttributeRepository productModelAttributeRepository;

    private final String RECOMMEND_API = "https://3187062a24cc.ngrok-free.app/recommend"; // ngrok 주소

    private int getMostUsedOption(String attributeName) {
        List<ProductModelAttribute> all = productModelAttributeRepository.findAll();

        return all.stream()
                .filter(attr -> attributeName.equals(attr.getAttributeValue().getAttribute().getName()))
                .collect(Collectors.groupingBy(
                        attr -> attr.getAttributeValue().getId(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0L)
                .intValue();
    }

    private int calculateAgeGroup(LocalDate birth) {
        if (birth == null) return 0;
        int age = LocalDate.now().getYear() - birth.getYear();
        if (age < 20) return 0;
        if (age < 30) return 1;
        if (age < 40) return 2;
        return 3;
    }

    public List<Product> getBestProducts() {
        return productRepository.findTopRecommendedProducts();
    }

    public List<Product> getRecommendedProducts(Long userId) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("k", 6);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(RECOMMEND_API, body, Map.class);
            List<Integer> ids = (List<Integer>) response.getBody().get("recommended");

            if (ids == null || ids.isEmpty()) {
                return getBestProducts();
            }

            return productRepository.findAllById(ids.stream().map(Long::valueOf).toList());

        } catch (Exception e) {
            System.err.println("⚠️ 추천 서버 연결 실패, 인기 상품으로 fallback");
            return getBestProducts();
        }
    }
}