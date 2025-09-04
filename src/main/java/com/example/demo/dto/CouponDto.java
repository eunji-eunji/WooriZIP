package com.example.demo.dto;

import com.example.demo.constant.CouponType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDto {

    private Long id;
    private String name;
    private String code;
    private CouponType type;

    private Integer discountAmount;
    private Integer discountPercent;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean isActive;
    private int minOrderPrice;
    private int usageLimit;

}
