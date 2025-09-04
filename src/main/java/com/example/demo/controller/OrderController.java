package com.example.demo.controller;

import com.example.demo.dto.CartDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.entity.Order;
import com.example.demo.oauth2.CustomOAuth2User;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    // 로그 수집
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public String order(Authentication authentication, Model model,
                        @RequestParam(value = "selectedCouponId", required = false) Long selectedCouponId) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            OrderDto orderDto = orderService.getOrder(userDetails.getUser().getEmail());
            model.addAttribute("loginUser", userDetails.getUser());
            model.addAttribute("orderDto", orderDto);
            model.addAttribute("userCoupons", userService.getUserCoupons(userDetails.getUser()));
            model.addAttribute("selectedCouponId", selectedCouponId);
        } else if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User oauth2User) {
            OrderDto orderDto = orderService.getOrder(oauth2User.getUser().getEmail());
            model.addAttribute("loginUser", oauth2User.getUser());
            model.addAttribute("orderDto", orderDto);
            model.addAttribute("userCoupons", userService.getUserCoupons(oauth2User.getUser()));
            model.addAttribute("selectedCouponId", selectedCouponId);
        }
        return "order/orderPayment";
    }

    @PostMapping
    public String processOrder(@RequestParam("type") String orderType,
                               @RequestParam("cartItemIds") List<Long> cartItemIds,
                               @RequestParam(value = "couponId", required = false) Long couponId,
                               Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";

        log.info("들어온 값 : {}", cartItemIds);
        log.info("들어온 타입 : {}", orderType);
        log.info("선택된 쿠폰 : {}", couponId);

        OrderDto orderDto;
        if (orderType.equals("cart")) {
            orderDto = orderService.createOrder(email, cartItemIds, couponId);
            model.addAttribute("orderDto", orderDto);
        }
        
        // 쿠폰이 선택된 경우에만 selectedCouponId를 전달
        if (couponId != null) {
            return "redirect:/order?selectedCouponId=" + couponId;
        } else {
            return "redirect:/order";
        }
    }


    // 바로구매 처리
    @PostMapping("/now")
    public String orderNow(@ModelAttribute CartDto dto ,
                           Authentication authentication, Model model) {
        log.info("바로구매로 넘어온 값 : CartDto {}", dto.toString());

        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";

        Long productId = dto.getItems().get(0).getProductId(); // 내부에서 꺼냄
        OrderDto orderDto = orderService.createOrderByNow(dto, productId, email);
        log.info("바로구매로 넘어온 값 : 상품아이디 : {} , dto {}", productId, dto.toString());
        model.addAttribute("orderDto", orderDto);
        return "redirect:/order";
    }

    @GetMapping("/search")
    public String searchOrders(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        List<OrderDto> orders = orderService.findOrdersByDateRange(startDate, endDate);
        model.addAttribute("orders", orders);
        return "order/orderHistory";
    }


    // 주문확인
    @GetMapping("/history")
    public String history(Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        model.addAttribute("loginUser", UserUtils.getUser(authentication));

        List<Order> orders = orderService.findOrdersByUserEmail(email);
        model.addAttribute("orders", orders);

        return "order/orderHistory";
    }

    // 주문 내역 상세 보기
    @GetMapping("/detail")
    public String viewOrderDetail(@RequestParam("orderId") String orderId, Model model, Authentication authentication) {
        model.addAttribute("loginUser", UserUtils.getUser(authentication));

        OrderDto orderDto = orderService.getOrderByOrderIdForDetail(orderId);
        model.addAttribute("orderDto", orderDto);
        log.info("orderDto.item: {}", orderDto.getItems());
        return "order/orderCompleteDetail";
    }
}