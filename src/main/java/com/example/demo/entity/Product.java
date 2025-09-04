package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Where(clause = "is_deleted = false") // 0731 추가
public class Product { // 상품
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int price;

    private int stockQuantity; // 전체 재고 수량
    private double averageRating; // 후기 평점 평균

    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // 0731 dk 추가 논리적 삭제(soft delete)를 위해서

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductModel> productModels = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,  orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>(); // 0727 DK추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreatedDate
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createdAt;

    public void addProductModel(ProductModel model) {
        // 현재 리스트에 동일한 모델이 이미 존재하는지 확인
        // equals() 메서드 기준으로 비교 (모델명, 가격, 재고가 같은 경우 동일 객체로 판단)
        if (!this.productModels.contains(model)) {
            // 중복되지 않은 경우에만 리스트에 추가
            this.productModels.add(model);

            // 양방향 연관관계를 위해 모델에도 이 상품을 설정
            // → JPA가 두 객체 간의 관계를 정확하게 인식하도록 도와줌
            model.setProduct(this);
        }
    }

    public String getThumbnailUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return null;
    }

    // 0731 dk 추가
    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }
    public boolean isDeleted() {
        return isDeleted;
    }
}