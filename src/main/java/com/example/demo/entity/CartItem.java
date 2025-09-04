package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Setter
@Table(name = "cart_list")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 다른곳에서 new 생성자를 막기 위해
// 장바구니 상품리스트
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "product_model_id", nullable = false)
    private ProductModel productModel;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    private int count;

    public static CartItem createCartItem(Product product, ProductModel productModel, int count, Cart cart) {
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setProductModel(productModel);
        cartItem.setCount(count);
        cartItem.setCart(cart);
        return cartItem;
    }

    public void addCount(int count) {
        this.count += count;
    }

    public void  updateCount(int count){
        this.count = count;
    }
}