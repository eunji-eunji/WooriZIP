package com.example.demo.dto;


import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CartDto {
    private String email;
    // form 에서 받을 데이터
    private List<CartItemDto> items = new ArrayList<>();
    // 총 결제 금액
    private long totalPrice;

    // DB에서 가져올때
    public CartDto(Cart cart) {

        this.email = cart.getUser().getEmail();
        this.items = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            this.items.add(new CartItemDto(cartItem));
        }
        this.totalPrice = cart.getTotalPrice();
    }
}

