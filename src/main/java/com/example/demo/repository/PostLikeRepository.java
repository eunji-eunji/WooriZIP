package com.example.demo.repository;

import com.example.demo.entity.InteriorPost;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 좋아요 여부 확인용
    boolean existsByPostAndUser(InteriorPost post, Users user);

    // 좋아요 객체 가져오기 (토글용)
    Optional<PostLike> findByPostAndUser(InteriorPost post, Users user);

    void deleteByPost(InteriorPost post);

    int countByPost(InteriorPost post);
}
