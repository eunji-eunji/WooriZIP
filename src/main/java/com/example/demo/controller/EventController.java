package com.example.demo.controller;

import com.example.demo.dto.CouponDto;
import com.example.demo.service.CouponService;
import com.example.demo.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final CouponService couponService;

    @GetMapping()
    public String eventPAge(Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        Users loginUser = email != null ? (Users) UserUtils.getUser(authentication) : null;
        model.addAttribute("loginUser", loginUser);

        List<CouponDto> allCoupons = couponService.getAllCoupons();
        model.addAttribute("coupons", allCoupons);

        // 쿠폰별로 이미 받았는지 여부를 map으로 전달
        java.util.Map<Long, Boolean> receivedMap = new java.util.HashMap<>();
        if (loginUser != null) {
            for (CouponDto coupon : allCoupons) {
                receivedMap.put(coupon.getId(), couponService.hasUserReceivedCoupon(coupon.getId(), loginUser));
            }
        }
        model.addAttribute("receivedMap", receivedMap);
        return "/event/event";
    }

    @PostMapping("/coupons/get")
    public String getCoupon(@RequestParam Long couponId, RedirectAttributes redirectAttributes,
                            Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        model.addAttribute("loginUser", UserUtils.getUser(authentication));

        try {
            couponService.claimCoupon(couponId, email);
            redirectAttributes.addFlashAttribute("message", "쿠폰을 성공적으로 받았습니다!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/event";
    }

}
