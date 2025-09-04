package com.example.demo.controller;

import com.example.demo.dto.CategoryRequestDto;
import com.example.demo.dto.CategoryResponseDto;
import com.example.demo.dto.CategoryTreeDto;
import com.example.demo.dto.ProductListDto;
import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@RequestBody CategoryRequestDto dto) {
        CategoryResponseDto created = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAll() {
        List<CategoryResponseDto> list = categoryService.getAllCategories();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/parents")
    public ResponseEntity<List<CategoryResponseDto>> getParentCategories() {
        List<Category> parents = categoryRepository.findByParentIsNull(); // 대분류만
        List<CategoryResponseDto> response = parents.stream()
                .map(c -> new CategoryResponseDto(c.getId(), c.getName(), c.getDepth(), null))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/children")
    public ResponseEntity<List<CategoryResponseDto>> getChildCategories(@RequestParam Long parentId) {
        List<Category> children = categoryRepository.findByParentId(parentId);
        List<CategoryResponseDto> response = children.stream()
                .map(c -> new CategoryResponseDto(c.getId(), c.getName(), c.getDepth(), parentId))
                .toList();
        return ResponseEntity.ok(response);
    }

    // 계층 구조로 전체 카테고리 반환
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeDto>> getCategoryTree() {
        List<CategoryTreeDto> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }

    // 사용자용 목록리스트
    @GetMapping("/products")
    public String productList(@RequestParam(required = false) Long category, Model model) {
        List<ProductListDto> products = productService.findProducts(category);
        model.addAttribute("products", products);
        return "products"; // 상품 목록 템플릿
    }

}
