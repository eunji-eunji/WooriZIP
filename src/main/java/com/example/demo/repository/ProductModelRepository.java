package com.example.demo.repository;

import com.example.demo.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {

    @Query("SELECT pm FROM ProductModel pm WHERE pm.product.id = :productId")
    List<ProductModel> findAllByProduct_ProductId(@Param("productId") Long productId);

}
