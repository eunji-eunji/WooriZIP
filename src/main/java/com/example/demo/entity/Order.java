package com.example.demo.entity;

import com.example.demo.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Setter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 다른곳에서 new 생성자를 막기 위해
public class Order {
    // 주문 저장 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // 주문번호
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users users; // 주문한 고객 아이디

    @Column(name = "order_date",nullable = false)
    private LocalDateTime orderDate; // 주문일

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문상태 저장

    @Column(name = "payInfo")
    private  String payInfo; // 결제 방식

    @Column(name = "payment_method")
    private String paymentMethod; // 결제 방법

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId; // 주문 고유 ID

    // 적용 쿠폰
    @ManyToOne
    @JoinColumn(name = "user_coupon_id")
    private UserCoupon userCoupon;

    private int totalPrice;   // 상품 총 금액
    private int discountAmount;
    private int deliveryFee;  // 배송비
    private int finalAmount;  // 최종 결제 금액 (쿠폰 적용 후)

    // 주문 목록
    @OneToMany(mappedBy = "order", cascade=CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 양방향
    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem); // 주문 목록을 주문에 추가
        orderItem.setOrder(this); // 주문 항목의 주문을 현재 주문으로 설정
    }

    // 주문 테이블 생성
    public static Order createOrder(Users user, List<OrderItem> orderItems) {
        Order order = new Order();
        order.setUsers(user);

        // 주문아이템이 한개가 아닐수도 있기때문에 for문으로 집어넣기 => 장바구니에서 주문시
        for (OrderItem item : orderItems) {
            order.addOrderItem(item);
        }

        order.setOrderStatus(OrderStatus.STAY); // 주문 상태
        order.setOrderDate(LocalDateTime.now()); // 결제 날짜
        order.setOrderId(UUID.randomUUID().toString()); // 고유한 주문 ID 생성

        return order;
    }

    public void updatePaymentInfo(String paymentMethod,String payInfo) {
        this.orderStatus = OrderStatus.ORDER; // 성공시 결제 상태를 변경
        this.payInfo = payInfo; // 결제 방식 추가
        this.paymentMethod = paymentMethod; // 결제 수단을 업데이트
    }

}