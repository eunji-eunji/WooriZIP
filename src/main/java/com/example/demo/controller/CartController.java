package com.example.demo.controller;

import com.example.demo.dto.CartDto;
import com.example.demo.entity.Users;
import com.example.demo.service.CartService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    // /cart 시 장바구니
    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        Users loginUser = (Users) UserUtils.getUser(authentication);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("userCoupons", userService.getUserCoupons(loginUser));
        CartDto cart = cartService.getCartByEmail(email);
        // 만약 없다면 빈 객체 생성하여 전달
        if (cart == null) {
            cart = new CartDto();
        }
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, CartDto dto, Authentication authentication, Model model) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";

        try {
            cartService.addItemToCart(dto, email, productId);
            return "redirect:/cart"; // 장바구니 페이지로 리다이렉션
        } catch (Exception e) {
            log.error("Error adding item to cart", e);
            model.addAttribute("error", "Failed to add item to cart");
            return "/error";
        }
    }

    // 업데이트
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCount(@RequestBody Map<String, Object> payload) {
        Long cartItemId = Long.valueOf(payload.get("cartItemId").toString());
        int count = Integer.parseInt(payload.get("count").toString());

        Map<String, Object> response = new HashMap<>();
        if(count < 1){
            response.put("success", false);
            response.put("message", "수량은 1보다 작을 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }
        // 해당 재고 가져오기
        int stock = cartService.getItemStock(cartItemId);
        log.info("재고 : {} ",stock );
        if (count > stock) {
            response.put("success", false);
            response.put("message", "주문하려는 수량이 재고보다 많습니다!.");
            return ResponseEntity.badRequest().body(response);
        }
        cartService.updateCartItemQuantity(cartItemId, count);
        response.put("success", true);
        response.put("cartItemId", cartItemId);
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // 카트 아이템 개별 삭제 메소드
    @PostMapping("/remove")
    public String removeItemFromCart(@RequestParam Long cartItemId, Authentication authentication, Model model) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";

        try {
            cartService.removeItemFromCart(email, cartItemId);
            return "redirect:/cart"; // 장바구니 페이지로 리다이렉션
        } catch (Exception e) {
            log.error("Error removing item from cart", e);
            model.addAttribute("error", "Failed to remove item from cart");
            return "error";
        }
    }
    
    // 전체 삭제
    @PostMapping("/clear")
    public String clearCart(Authentication authentication, Model model) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";

        try {
            cartService.clearCart(email);
            return "redirect:/cart"; // 장바구니 페이지로 리다이렉션
        } catch (Exception e) {
            log.error("Error clearing cart", e);
            model.addAttribute("error", "Failed to clear cart");
            return "error";
        }
    }

    // 선택 항목 삭제
    @PostMapping("/deleteSelected")
    @ResponseBody
    public ResponseEntity<?> deleteSelectedItems(@RequestBody List<Long> cartItemIds) {
        cartService.deleteSelectedItems(cartItemIds);
        return ResponseEntity.ok().build();
    }
}