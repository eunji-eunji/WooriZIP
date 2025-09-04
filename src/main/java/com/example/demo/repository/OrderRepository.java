package com.example.demo.repository;

import com.example.demo.constant.OrderStatus;
import com.example.demo.entity.Order;
import com.example.demo.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
        SELECT DISTINCT o FROM Order o
        JOIN FETCH o.users
        JOIN FETCH o.orderItems oi
        JOIN FETCH oi.product p
        JOIN FETCH oi.productModel pm
        ORDER BY o.orderDate DESC
    """)
    List<Order> findAllWithDetailsForAdmin();

    Page<Order> findAll(Pageable pageable);

    @Query(value = """
        WITH RECURSIVE dates AS (
        SELECT CURDATE() - INTERVAL 6 DAY AS date
        UNION ALL
        SELECT date + INTERVAL 1 DAY FROM dates WHERE date + INTERVAL 1 DAY <= CURDATE()
        )
        SELECT d.date AS date, COUNT(o.id) AS count
        FROM dates d
        LEFT JOIN orders o ON DATE(o.order_date) = d.date
        AND o.order_status = 'ORDER'
        GROUP BY d.date
        ORDER BY d.date
    """, nativeQuery = true)
    List<Object[]> getOrderCountLast7Days();

    @Query(value = """
        WITH RECURSIVE category_hierarchy AS (
            SELECT id, name, parent_id, name AS top_category
            FROM category
            WHERE parent_id IS NULL
            UNION ALL
            SELECT c.id, c.name, c.parent_id, ch.top_category
            FROM category c
            JOIN category_hierarchy ch ON c.parent_id = ch.id
        )
        SELECT DATE_FORMAT(CURRENT_DATE(), '%Y-%m') AS month,
               ch.top_category AS category,
               IFNULL(SUM(oi.count), 0) AS count
        FROM category_hierarchy ch
        INNER JOIN product p ON p.category_id = ch.id
        INNER JOIN order_item oi ON oi.product_id = p.id
        INNER JOIN `orders` o ON oi.order_id = o.id
        WHERE o.order_status = 'ORDER'
          AND YEAR(o.order_date) = YEAR(CURRENT_DATE())
          AND MONTH(o.order_date) = MONTH(CURRENT_DATE())
        GROUP BY ch.top_category
        ORDER BY ch.top_category ASC
    """, nativeQuery = true)
    List<Object[]> getThisMonthCategorySales();

    @Query(value = """
        WITH RECURSIVE category_hierarchy AS (
        SELECT id, name, parent_id, name AS top_category
        FROM category
        WHERE parent_id IS NULL
        UNION ALL
        SELECT c.id, c.name, c.parent_id, ch.top_category
        FROM category c
        JOIN category_hierarchy ch ON c.parent_id = ch.id
        )
        SELECT ch.top_category AS category,
        IFNULL(SUM(oi.count), 0) AS count
        FROM category_hierarchy ch
        LEFT JOIN product p ON p.category_id = ch.id
        LEFT JOIN order_item oi ON oi.product_id = p.id
        LEFT JOIN orders o ON o.id = oi.order_id
        AND o.order_status = 'ORDER'
        GROUP BY ch.top_category
        ORDER BY ch.top_category ASC
    """, nativeQuery = true)
    List<Object[]> getTotalCategorySales();
}
