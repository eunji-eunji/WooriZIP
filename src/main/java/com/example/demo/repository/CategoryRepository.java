package com.example.demo.repository;

import com.example.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 선택적: depth별 조회
    List<Category> findByDepth(int depth);

    // 선택적: 특정 부모 카테고리의 자식 카테고리들 조회
    List<Category> findByParentId(Long parentId); // 하위 분류
    List<Category> findByParentIsNull(); // 대분류용

    // 선택적: 카테고리 이름으로 검색
    List<Category> findByNameContaining(String keyword);

    // 정확한 카테고리 이름으로 검색
    java.util.Optional<Category> findByName(String name);

}
