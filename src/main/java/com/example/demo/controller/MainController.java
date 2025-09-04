package com.example.demo.controller;

import com.example.demo.dto.CouponDto;
import com.example.demo.dto.InteriorPostDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.entity.Users;
import com.example.demo.service.CouponService;
import com.example.demo.service.InteriorPostService;
import com.example.demo.service.RecommendService;
import com.example.demo.service.ReviewPostService;
import com.example.demo.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final InteriorPostService interiorPostService;
    private final ReviewPostService reviewPostService;
    private final RecommendService recommendService;
    private final CouponService couponService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/")
    public String welcome(Authentication authentication, Model model) {
        Users user = null;
        if (authentication != null) {
            user = (Users) UserUtils.getUser(authentication);
            model.addAttribute("loginUser", user);
        }

        // 추천 상품 추가
        if (user != null) {
            List<Product> recommended = recommendService.getRecommendedProducts(user.getId());
            try {
                model.addAttribute("products", recommended);
            } catch (Exception e) {
                model.addAttribute("products", recommendService.getBestProducts());
            }
        } else {
            model.addAttribute("products", recommendService.getBestProducts());
        }

        // 전체 게시글 가져오기
        List<InteriorPostDto> allPosts = interiorPostService.findAll();

        // 공지사항과 일반 게시글 분리
        List<InteriorPostDto> notices = allPosts.stream()
                .filter(InteriorPostDto::isNotice)
                .collect(Collectors.toList());

        List<InteriorPostDto> regularPosts = allPosts.stream()
                .filter(post -> !post.isNotice())
                .limit(3)
                .collect(Collectors.toList());

        model.addAttribute("notice", notices.isEmpty() ? null : notices.get(0));
        model.addAttribute("latestInteriorPosts", regularPosts);

        // 최신 리뷰 4개
        model.addAttribute("latestReviews",
                reviewPostService.findAllReviews().stream()
                        .limit(4)
                        .collect(Collectors.toList()));

        // 이벤트 쿠폰 데이터 추가 (event.html과 동일한 방식)
        List<CouponDto> allCoupons = couponService.getAllCoupons();
        model.addAttribute("coupons", allCoupons);

        // 쿠폰별로 이미 받았는지 여부를 map으로 전달 (event.html과 동일)
        java.util.Map<Long, Boolean> receivedMap = new java.util.HashMap<>();
        if (user != null) {
            for (CouponDto coupon : allCoupons) {
                receivedMap.put(coupon.getId(), couponService.hasUserReceivedCoupon(coupon.getId(), user));
            }
        }
        model.addAttribute("receivedMap", receivedMap);

        // 카테고리 데이터 추가 (탭 링크용)
        List<Category> categories = categoryRepository.findByParentIsNull(); // 대분류만
        model.addAttribute("categories", categories);

        return "welcome";
    }

}
