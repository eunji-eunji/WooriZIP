package com.example.demo.service;

import com.example.demo.dto.CategoryRequestDto;
import com.example.demo.dto.CategoryResponseDto;
import com.example.demo.dto.CategoryTreeDto;
import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        Category parent = null;
        int depth = 0;

        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 카테고리 없음"));
            depth = parent.getDepth() + 1;
        }

        Category category = Category.builder()
                .name(dto.getName())
                .depth(depth)
                .parent(parent)
                .build();

        categoryRepository.save(category);

        return new CategoryResponseDto(category.getId(), category.getName(), category.getDepth(),
                category.getParent() != null ? category.getParent().getId() : null);
    }

    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponseDto(
                        c.getId(), c.getName(), c.getDepth(),
                        c.getParent() != null ? c.getParent().getId() : null))
                .collect(Collectors.toList());
    }

    public List<CategoryTreeDto> getCategoryTree() {
        List<Category> all = categoryRepository.findAll();

        Map<Long, CategoryTreeDto> map = new HashMap<>();
        List<CategoryTreeDto> roots = new ArrayList<>();

        for (Category category : all) {
            CategoryTreeDto dto = new CategoryTreeDto(category.getId(), category.getName(), new ArrayList<>());
            map.put(category.getId(), dto);
        }

        for (Category category : all) {
            CategoryTreeDto dto = map.get(category.getId());
            if (category.getParent() == null) {
                roots.add(dto);
            } else {
                map.get(category.getParent().getId()).getChildren().add(dto);
            }
        }

        return roots;
    }

}
