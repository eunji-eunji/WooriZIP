package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryIdIn(List<Long> categoryIds);

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.category c " +
            "LEFT JOIN FETCH c.parent cp " +
            "LEFT JOIN FETCH cp.parent cpp " +
            "WHERE p.id = :id")
    Product findWithCategoryTreeById(@Param("id") Long id);

    List<Product> findByIdIn(List<Long> myWishList);

    Page<Product> findByIsDeletedFalse(Pageable pageable);  // ✅ 추가

    // 유저가 보는 상품 상세
    List<Product> findByCategoryIdInAndIsDeletedFalse(List<Long> categoryIds);

    // 챗봇용 메서드들
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p ORDER BY p.price ASC")
    List<Product> findTop5ByOrderByPriceAsc(Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findTop5ByOrderByCreatedAtDesc(Pageable pageable);

    List<Product> findAllByOrderByCreatedAtDesc();
    
    // 기본 메서드들 (Pageable 없이)
    default List<Product> findTop5ByOrderByPriceAsc() {
        return findTop5ByOrderByPriceAsc(Pageable.ofSize(5));
    }


    @Query(value = """
        SELECT p.* FROM product p
        JOIN (
            SELECT product_id, COUNT(*) AS cnt
            FROM recommend_log
            WHERE product_id IS NOT NULL
            GROUP BY product_id
            ORDER BY cnt DESC
            LIMIT 6
        ) AS top ON p.id = top.product_id
        WHERE p.is_deleted = 0
        """, nativeQuery = true)
    List<Product> findTopRecommendedProducts();
}
