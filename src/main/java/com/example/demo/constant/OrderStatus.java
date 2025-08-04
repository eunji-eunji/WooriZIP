package com.example.demo.constant;

public enum OrderStatus {
    // 주문 임시 저장 상태 // 결제대기 /  주문완료 /  주문 취소 / 배송중 / 배송완료
    TEMP,   // 주문페이지 진입
    STAY,   // 결제하기 버튼 클릭
    ORDER,
    CANCEL,
    SHIPPED,
    COMPLETED
}
