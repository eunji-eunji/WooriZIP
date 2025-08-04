package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostCommentDto {
    private Long commentId;
    private Long postId;
    private String email;      // 작성자 식별용
    private String nickname;   // 작성자 표시용
    private String content;
    private LocalDateTime createdAt;
}
