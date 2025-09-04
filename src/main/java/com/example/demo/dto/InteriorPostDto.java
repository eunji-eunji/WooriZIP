package com.example.demo.dto;

import com.example.demo.entity.InteriorPost;
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
public class    InteriorPostDto {
    private Long postId;
    private String title;
    private String content;

    private List<MultipartFile> files;
    private String fileNames;
    private String filePaths;
    private List<String> filePathList;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String email;
    private String nickname;

    private int views;
    private int liked;
    private boolean likedByCurrentUser;
    private boolean isNotice;

    public static InteriorPostDto fromEntity(InteriorPost post) {
        List<String> filePathList = null;
        if (post.getFilePath() != null && !post.getFilePath().isEmpty()) {
            filePathList = Arrays.stream(post.getFilePath().split(","))
                    .map(String::trim)
                    .filter(path -> !path.equals("/images/default-image.jpg")) // 디폴트 이미지 제외
                    .collect(Collectors.toList());
            
            // 필터링 후 리스트가 비어있으면 null로 설정
            if (filePathList.isEmpty()) {
                filePathList = null;
            }
        }

        return InteriorPostDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .fileNames(post.getFileName())
                .filePaths(post.getFilePath())
                .filePathList(filePathList)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .email(post.getUser().getEmail())
                .nickname(post.getUser().getNickname())
                .liked(post.getLiked())
                .views(post.getViews())
                .isNotice(post.isNotice())
                .build();
    }
}