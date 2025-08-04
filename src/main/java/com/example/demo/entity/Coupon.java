package com.example.demo.entity;

import com.example.demo.constant.CouponType;
import com.example.demo.dto.CouponDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    private int discountAmount;
    private int discountPercent;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean isActive;

    private int minOrderPrice;
    private int usageLimit;

    @Builder
    public Coupon(String name, String code, CouponType type,
                  int discountAmount, int discountPercent,
                  LocalDate startDate, LocalDate endDate,
                  boolean isActive, int minOrderPrice, int usageLimit) {
        this.name = name;
        this.code = code;
        this.type = type;
        this.discountAmount = discountAmount;
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.minOrderPrice = minOrderPrice;
        this.usageLimit = usageLimit;
    }

    public static Coupon createCoupon(CouponDto dto) {
        return Coupon.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .type(dto.getType())
                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : 0)
                .discountPercent(dto.getDiscountPercent() != null ? dto.getDiscountPercent() : 0)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isActive(dto.isActive())
                .minOrderPrice(dto.getMinOrderPrice())
                .usageLimit(dto.getUsageLimit())
                .build();
    }

}
