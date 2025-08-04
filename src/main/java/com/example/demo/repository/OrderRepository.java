package com.example.demo.repository;

import com.example.demo.constant.OrderStatus;
import com.example.demo.entity.Order;
import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId); // 주문아이디로 찾기
    List<Order> findByUsersAndOrderStatus(Users user, OrderStatus orderStatus);//
    List<Order> findAllByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 사용자가 특정 상품을 구매했는지 확인
    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems oi WHERE o.users.email = :email AND o.orderStatus = :orderStatus AND oi.product.id = :productId")
    boolean existsByUsersEmailAndOrderStatusAndOrderItemsProductId(@Param("email") String email, @Param("orderStatus") OrderStatus orderStatus, @Param("productId") Long productId);

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.orderItems oi " +
            "JOIN FETCH oi.product p " +
            "JOIN FETCH oi.productModel pm " +
            "WHERE o.orderId = :orderId")
    Optional<Order> findWithDetailsByOrderId(@Param("orderId") String orderId);

    List<Order> findByUsersAndOrderStatusOrderByOrderDateDesc(Users user, OrderStatus orderStatus);

    List<Order> findByUsers(Users user);
}
