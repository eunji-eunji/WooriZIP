package com.example.demo.repository;

import com.example.demo.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByIsActiveTrue();

    @Transactional
    @Modifying
    @Query("UPDATE Coupon c SET c.isActive = false WHERE c.endDate < :today AND c.isActive = true")
    int deactivateExpiredCoupons(@Param("today") LocalDate today);

    Optional<Coupon> findByCode(String code);
}