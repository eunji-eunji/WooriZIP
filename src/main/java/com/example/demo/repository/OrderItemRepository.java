package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    void deleteByProductModelIn(List<ProductModel> productModels); // 0721 해당 상품 관련 product_id가 있는 행 삭제 상품관리

    // 해당 옵션 ID를 참조하는 주문 항목이 존재하는지 확인
    boolean existsByProductModelId(Long productModelId); // 0729 dk추가

    void deleteAllByOrder(Order order);
}
