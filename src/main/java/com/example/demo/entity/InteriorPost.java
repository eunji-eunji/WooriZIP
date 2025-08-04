package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;


@Entity
@Table(name = "interior_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteriorPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;

    private String fileName;
    private String filePath;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isNotice = false;  // 공지사항 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // users 테이블의 PK 컬럼명에 맞게!
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    private int liked;
    private int views;

    @PrePersist
    public void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
