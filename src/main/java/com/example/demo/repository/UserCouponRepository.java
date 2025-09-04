package com.example.demo.repository;

import com.example.demo.entity.Coupon;
import com.example.demo.entity.UserCoupon;
import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserAndCoupon(Users user, Coupon coupon);
    List<UserCoupon> findByUserAndUsedFalse(Users user);
}
