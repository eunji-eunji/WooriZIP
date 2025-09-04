package com.example.demo.dto;

import com.example.demo.entity.CartItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CartItemDto {
    private Long productId;
    private Long cartItemId; // 카트 번호
    private Long modelId; // 모델 아이디
    private String modelName;
    private String productName; // 제품이름
    private int count; // 개수
    private int price; // 가격
    private String imgUrl;
    private String productModelSelect;
    private boolean isDeleted;
    private int stock;  // 남은 재고 수량

    public CartItemDto(CartItem cartItem) {
        this.cartItemId = cartItem.getId();
        this.productName = cartItem.getProduct().getName();
        this.productId = cartItem.getProduct().getId();
        this.modelId =  cartItem.getProductModel().getId();
        this.count = cartItem.getCount();
        this.price = cartItem.getProductModel().getPrice();
        this.imgUrl = cartItem.getProduct().getImages().get(0).getImageUrl();
        this.productModelSelect = cartItem.getProductModel().getProductModelSelect();
        this.isDeleted = cartItem.getProduct().isDeleted();
        this.stock = cartItem.getProductModel().getPrStock();
    }
}