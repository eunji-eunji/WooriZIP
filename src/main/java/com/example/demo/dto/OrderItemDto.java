package com.example.demo.dto;

import com.example.demo.entity.OrderItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class OrderItemDto {

    private Long productId;
    private Long orderItemId; // 주문아이템들의 아이디
    private String productName; // 제품이름
    private Long modelId; // 모델 아이디
    private int count; // 개수
    private int price; // 가격
    private String imgUrl;
    private int deliveryFee; // 배송비
    private String deliveryType; // 배송 구분

    private Long cartItemId;

    public OrderItemDto(OrderItem orderItem) {
        this.productId = orderItem.getProduct().getId();
        this.orderItemId = orderItem.getId();
        this.productName = orderItem.getProduct().getName();
        this.modelId =  orderItem.getProductModel().getId();
        this.count = orderItem.getCount();
        this.price = orderItem.getProductModel().getPrice(); // 옵션별 가격으로 수정
        this.imgUrl = orderItem.getProduct().getImages().get(0).getImageUrl();
        this.deliveryFee = 0; // 기본 배송비
        this.deliveryType = "무료배송"; // 기본 배송 구분
    }
}
