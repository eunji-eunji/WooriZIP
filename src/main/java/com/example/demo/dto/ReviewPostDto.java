package com.example.demo.dto;

import com.example.demo.entity.ReviewPost;
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
public class ReviewPostDto {

    private Long id;
    private String title;
    private String content;
    private int rating;

    // 파일 업로드 관련
    private List<MultipartFile> files;
    private String fileNames;
    private String filePaths;
    private List<String> filePathList;

    private String email;
    private String nickname;

    private Long productId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewPostDto fromEntity(ReviewPost post) {
        List<String> filePathList = null;
        if (post.getFilePaths() != null && !post.getFilePaths().isEmpty()) {
            filePathList = Arrays.stream(post.getFilePaths().split(","))
                    .map(String::trim)
                    .filter(path -> !path.isEmpty())
                    .collect(Collectors.toList());
        }

        return ReviewPostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .rating(post.getRating())
                .fileNames(post.getFileNames())
                .filePaths(post.getFilePaths())
                .filePathList(filePathList)
                .email(post.getEmail())
                .nickname(post.getNickname())
                .productId(post.getProduct().getId())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
