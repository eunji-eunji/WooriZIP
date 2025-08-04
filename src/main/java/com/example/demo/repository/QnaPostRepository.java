package com.example.demo.repository;

import com.example.demo.entity.QnaPost;
import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QnaPostRepository extends JpaRepository<QnaPost, Long> {

    //총 문의수
    @Query("SELECT COUNT(q) FROM QnaPost q WHERE q.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(q) FROM QnaPost q WHERE q.product.id = :productId AND q.createdAt >= (SELECT p.createdAt FROM QnaPost p WHERE p.id = :postId)")
    long countPositionInProduct(@Param("postId") Long postId, @Param("productId") Long productId);

    //페이지네이션
    @Query(value = "SELECT * FROM qna_post WHERE product_id = :productId ORDER BY created_at DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<QnaPost> findByProductIdWithPaging(Long productId, int offset, int limit);

    List<QnaPost> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<QnaPost> findByEmailOrderByCreatedAtDesc(String email);

    // 특정 QnA 게시글의 페이지 번호를 계산하는 쿼리
    @Query(value = """
           WITH post_position AS (
               SELECT ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY created_at DESC) - 1 as position
               FROM qna_post 
               WHERE product_id = :productId AND id = :postId
           )
           SELECT FLOOR(position/5) + 1 as page_number 
           FROM post_position
           """, nativeQuery = true)
    int getQnaPageNumber(@Param("productId") Long productId, @Param("postId") Long postId);

    void deleteByProduct(Product product);// 0721 해당 상품 관련 product_id가 있는 행 삭제 상품관리

    // 사용자 이메일로 QnA 글 삭제
    void deleteByEmail(String email);
}
