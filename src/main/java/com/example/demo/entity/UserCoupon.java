package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @ManyToOne
    private Coupon coupon;

    @Column(nullable = false)
    private boolean used;

    private LocalDateTime assignedAt;

    @Builder
    public UserCoupon(Users user, Coupon coupon, boolean used) {
        this.user = user;
        this.coupon = coupon;
        this.used = used;
        this.assignedAt = LocalDateTime.now();
    }
}
