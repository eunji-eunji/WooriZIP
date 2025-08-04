package com.example.demo.dto;

import com.example.demo.entity.QnaPost;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaPostDto {

    private Long id;
    private String title;
    private String content;

    // 파일 첨부 관련
    private List<MultipartFile> files;
    private String fileNames;
    private String filePaths;
    private List<String> filePathList;
    private String deleteImages; // 삭제할 이미지 경로들 (쉼표로 구분)

    private String email;
    private String nickname;
    private Long productId;

    private boolean isSecret = false; // 비밀글 여부

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 답변 포함 (DTO로)
    private QnaAnswerDto answer;

    private boolean answered;
    private String productName;
    private boolean purchased; // 구매 여부

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public static QnaPostDto fromEntity(QnaPost post) {
        List<String> filePathList = null;
        if (post.getFilePaths() != null && !post.getFilePaths().isEmpty()) {
            filePathList = Arrays.stream(post.getFilePaths().split(","))
                    .map(String::trim)
                    .filter(path -> !path.isEmpty())
                    .collect(Collectors.toList());
        }

        // 답변 정보 설정
        QnaAnswerDto answerDto = null;
        if (post.getAnswer() != null) {
            answerDto = QnaAnswerDto.builder()
                    .id(post.getAnswer().getId())
                    .content(post.getAnswer().getContent())
                    .createdAt(post.getAnswer().getCreatedAt())
                    .build();
        }

        return QnaPostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .fileNames(post.getFileNames())
                .filePaths(post.getFilePaths())
                .filePathList(filePathList)
                .email(post.getEmail())
                .nickname(post.getNickname())
                .productId(post.getProduct() != null ? post.getProduct().getId() : null)
                .isSecret(post.isSecret())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .answer(answerDto)
                .answered(post.getAnswer() != null)
                .build();
    }
}
