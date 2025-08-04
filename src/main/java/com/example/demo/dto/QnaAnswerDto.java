package com.example.demo.dto;

import com.example.demo.entity.QnaAnswer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaAnswerDto {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long qnaPostId;  // 어떤 질문에 대한 답변인지 명시

    // Entity → DTO
    public static QnaAnswerDto fromEntity(QnaAnswer answer) {
        return QnaAnswerDto.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .qnaPostId(answer.getQnaPost().getId())
                .build();
    }
}
