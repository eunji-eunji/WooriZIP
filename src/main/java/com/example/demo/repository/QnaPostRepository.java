package com.example.demo.repository;

import com.example.demo.entity.QnaPost;
import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QnaPostRepository extends JpaRepository<QnaPost, Long> {

    //페이지네이션
    @Query(value = "SELECT * FROM qna_post WHERE product_id = :productId ORDER BY created_at DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<QnaPost> findByProductIdWithPaging(Long productId, int offset, int limit);

    List<QnaPost> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<QnaPost> findByEmailOrderByCreatedAtDesc(String email);

    // 사용자 이메일로 QnA 글 삭제
    void deleteByEmail(String email);
}
