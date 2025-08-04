package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {
    @Query("SELECT pm FROM ProductModel pm WHERE pm.product.id = :productId")
    List<ProductModel> findByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM ProductModel pm WHERE pm.product.id IS NULL")
    void deleteByPrIdIsNull();

    void deleteByProduct(Product product);

    @Query("SELECT pm FROM ProductModel pm WHERE pm.product.id = :productId")
    List<ProductModel> findAllByProduct_ProductId(@Param("productId") Long productId);

}
