package com.example.demo.repository;

import com.example.demo.entity.ReviewPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewPostRepository extends JpaRepository<ReviewPost, Long> {

    // 페이지네이션 없이 전체 리뷰 조회용
    List<ReviewPost> findByProductId(Long productId);

    // 페이지네이션용
    Page<ReviewPost> findByProductId(Long productId, Pageable pageable);

    // 전체 리뷰 최신순 조회
    Page<ReviewPost> findAll(Pageable pageable);

    boolean existsByProductIdAndEmail(Long productId, String email); // 1인 1리뷰 제한용
    List<ReviewPost> findByEmailOrderByCreatedAtDesc(String email);

    // product_id가 일치하는 리뷰를 모두 삭제
    void deleteByProduct(com.example.demo.entity.Product product);

    int countByProductId(Long productId); // 0727 dk추가

    // 사용자 이메일로 리뷰 글 삭제
    void deleteByEmail(String email);
}
