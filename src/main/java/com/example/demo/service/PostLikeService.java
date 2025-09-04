package com.example.demo.service;

import com.example.demo.entity.InteriorPost;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.Users;
import com.example.demo.repository.InteriorPostRepository;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final InteriorPostRepository postRepository;
    private final LoginRepository loginRepository;

    public String toggleLike(Long postId, String userEmail) {
        InteriorPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Users user = loginRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 좋아요 존재 여부 확인
        return postLikeRepository.findByPostAndUser(post, user).map(existing -> {
            // 이미 좋아요 → 취소
            postLikeRepository.delete(existing);
            post.setLiked(post.getLiked() - 1);
            postRepository.save(post);
            return "unliked";
        }).orElseGet(() -> {
            // 아직 좋아요 안함 → 등록
            PostLike like = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(like);
            post.setLiked(post.getLiked() + 1);
            postRepository.save(post);
            return "liked";
        });
    }

    // 좋아요 여부 체크
    public boolean hasUserLikedPost(InteriorPost post, Users user) {
        if (user == null) return false;
        return postLikeRepository.existsByPostAndUser(post, user);
    }
}
