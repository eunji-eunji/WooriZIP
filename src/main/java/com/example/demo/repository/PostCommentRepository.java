package com.example.demo.repository;

import com.example.demo.entity.InteriorPost;
import com.example.demo.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);
    void deleteByPost(InteriorPost post);
}
