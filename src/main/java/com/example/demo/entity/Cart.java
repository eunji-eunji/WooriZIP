package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

// 장바구니
@Getter
@Entity
@Table(name = "cart")
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 다른곳에서 new 생성자를 막기 위해
public class Cart {
    // 카트리스트 식별 키 =  id , 하나의 유저는 하나의 장바구니를 소유 = user 와 fk , 장바구니 목록들 = list , 총 금액
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();

    // 결제 금액
    public long getTotalPrice(){
        long totalPrice = 0L;
        for(CartItem item : cartItems){
            totalPrice += (long) item.getCount() * item.getProduct().getPrice();
        }
        return totalPrice;
    }

    // Cart 객체 생성 메서드
    public static Cart createCart(Users user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cart;
    }

    // 양방향
    public  void addCartItems(CartItem cartItem){
        this.cartItems.add(cartItem); // 해당 장바구니에 장바구니 리스트를 추가
        cartItem.setCart(this); // 카트 리스트에 해당 장바구니를 넣어줌으로써 양뱡향 처리를함
    }

    // 카트 아이템 삭제
    public void  removeItems(CartItem cartItem){
        this.cartItems.remove(cartItem);
        cartItem.setCart(null);
    }
}