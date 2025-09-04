package com.example.demo.service;

import com.example.demo.dto.PostCommentDto;
import com.example.demo.entity.InteriorPost;
import com.example.demo.entity.PostComment;
import com.example.demo.entity.Users;
import com.example.demo.repository.InteriorPostRepository;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository repo;
    private final InteriorPostRepository postRepository;
    private final LoginRepository loginRepository;

    /** 댓글 저장 */
    public void save(PostCommentDto dto) {
        Users user = loginRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        InteriorPost post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        PostComment entity = PostComment.builder()
                .content(dto.getContent())
                .post(post)
                .user(user)
                .build();

        repo.save(entity);
    }

    /** 댓글 목록 조회 */
    public List<PostCommentDto> findByPostId(Long postId) {
        return repo.findByPost_PostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** 댓글 삭제 */
    public void delete(Long id) {
        repo.deleteById(id);
    }


    /** 댓글 조회 */
    public PostCommentDto findById(Long id) {
        return repo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));
    }

    /** 댓글 수정 */
    public void update(PostCommentDto dto) {
        PostComment comment = repo.findById(dto.getCommentId())
                .orElseThrow(() -> new RuntimeException("댓글 없음"));
        comment.setContent(dto.getContent());
        repo.save(comment);
    }

    /** Entity → DTO 변환 */
    private PostCommentDto toDto(PostComment c) {
        return PostCommentDto.builder()
                .commentId(c.getCommentId())
                .postId(c.getPost().getPostId())
                .email(c.getUser().getEmail())
                .nickname(c.getUser().getNickname())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
