package com.example.demo.repository;

import com.example.demo.entity.ProductDetail;
import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {

    // Product ID로 ProductDetail 조회
    Optional<ProductDetail> findByProductId(Long productId);

    // Product 객체로 ProductDetail 조회
    Optional<ProductDetail> findByProduct(Product product);

    // Product ID로 ProductDetail 존재 여부 확인
    boolean existsByProductId(Long productId);

    void deleteByProduct(Product product); // 0721 해당 상품 관련 product_id가 있는 행 삭제 상품관리
} 