package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@ToString
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 다른곳에서 new 생성자를 막기 위해
public class OrderItem {
    // 주문상세
    // 하나의 주문에는 여러개의 주문아이템이 존재 상품 하나의 여러개 주문아이템 존재
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_item_id")
    private Long id; // 고유 번호

    // 상품 고유 번호 가져오기
    @ManyToOne(fetch = FetchType.LAZY) // 지연로딩
    @org.hibernate.annotations.NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "product_id")
    private Product product; // 제품 아이디 조인

    @ManyToOne(fetch = FetchType.LAZY) // 지연로딩
    @org.hibernate.annotations.NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "product_model_id") // 0729 dk 수정
    private ProductModel productModel; // 제품 모델 조인

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order; // 주문번호

    private int orderPrice; // 주문가격

    private int count; // 주문수량

    public int getTotalPrice() {
        return orderPrice * count;
    }

    // 주문상세 객체 만들고 객체를 리턴
    public static OrderItem createOrderItems(Product product, ProductModel productModel, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setProductModel(productModel);
        orderItem.setCount(count);
        orderItem.setOrderPrice(productModel.getPrice()); // 옵션별 가격으로 저장
        productModel.removeStock(count);
        return orderItem;
    }

}
