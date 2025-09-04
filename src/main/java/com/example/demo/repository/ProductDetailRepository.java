package com.example.demo.repository;

import com.example.demo.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {

    // Product ID로 ProductDetail 조회
    Optional<ProductDetail> findByProductId(Long productId);

    // Product ID로 ProductDetail 존재 여부 확인
    boolean existsByProductId(Long productId);

}