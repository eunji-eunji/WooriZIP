package com.example.demo.utils;

import com.example.demo.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

    private final CouponRepository couponRepository;

    /**
     * 매일 자정 00:00에 만료된 쿠폰 자동 비활성화
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void deactivateExpiredCoupons() {
        LocalDate today = LocalDate.now();
        int updatedCount = couponRepository.deactivateExpiredCoupons(today);
        System.out.println("[스케줄러] 만료된 쿠폰 비활성화 완료: " + updatedCount + "개");
    }
}
