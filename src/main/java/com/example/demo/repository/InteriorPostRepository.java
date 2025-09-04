package com.example.demo.repository;

import com.example.demo.entity.InteriorPost;
import com.example.demo.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InteriorPostRepository extends JpaRepository<InteriorPost, Long> {
    List<InteriorPost> findAllByOrderByCreatedAtDesc();
    Page<InteriorPost> findAll(Pageable pageable);
    List<InteriorPost> findByUserOrderByCreatedAtDesc(Users user);

    /** 제목으로 검색 */
    Page<InteriorPost> findByTitleContainingOrderByCreatedAtDesc(String title, Pageable pageable);
    
    /** 작성자로 검색 */
    @Query("SELECT p FROM InteriorPost p WHERE p.user.nickname LIKE %:nickname% ORDER BY p.createdAt DESC")
    Page<InteriorPost> findByUserNicknameContainingOrderByCreatedAtDesc(@Param("nickname") String nickname, Pageable pageable);
    
    /** 제목 또는 작성자로 검색 */
    @Query("SELECT p FROM InteriorPost p WHERE p.title LIKE %:keyword% OR p.user.nickname LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<InteriorPost> findByTitleContainingOrUserNicknameContainingOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);
}
