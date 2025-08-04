package com.example.demo.controller;

import com.example.demo.dto.CouponDto;
import com.example.demo.entity.Coupon;
import com.example.demo.service.CouponService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping()
    public String couponPage(Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        model.addAttribute("loginUser", UserUtils.getUser(authentication));

        List<CouponDto> allCoupons = couponService.getAllCoupons1();
        model.addAttribute("coupons", allCoupons);
        return "/admin/couponPage";
    }

    @PostMapping("/add")
    public String createCoupon(CouponDto dto) {
        couponService.create(dto);
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateCouponStatus(@PathVariable Long id,
                                                @RequestBody Map<String, Object> payload) {
        boolean isActive = Boolean.parseBoolean(payload.get("isActive").toString()); // 문자열로 안전 파싱
        couponService.updateIsActive(id, isActive);
        return ResponseEntity.ok(Map.of("success", true));
    }

}
