package com.example.demo.service;

import com.example.demo.constant.OrderStatus;
import com.example.demo.constant.CouponType;
import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductModelRepository productModelRepository;
    private final UserCouponRepository userCouponRepository;

    // 주문 목록 가져오기
    @Transactional(readOnly = true)
    public OrderDto getOrder(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다"));

        OrderDto orderDto = getExistingOrderDto(user);
        if (orderDto != null) {
            return orderDto;
        } else {
            return OrderDto.builder().build(); // 빈 주문 정보 반환
        }
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderId(String orderId) {
        // 최종 주문 완료한 것만 보여주기
        Order order = orderRepository.findWithDetailsByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());

        List<CartItem> cartItems = cartItemRepository.findByCartUserEmail(order.getUsers().getEmail());
        if (!cartItems.isEmpty()) {
            List<Long> orderedModelIds = order.getOrderItems().stream()
                    .map(oi -> oi.getProductModel().getId())
                    .toList();

            orderItemDtos = cartItems.stream()
                    .filter(cartItem -> orderedModelIds.contains(cartItem.getProductModel().getId()))
                    .map(cartItem -> {
                        OrderItemDto dto = new OrderItemDto();
                        dto.setProductId(cartItem.getProduct().getId());
                        dto.setOrderItemId(null);
                        dto.setProductName(cartItem.getProduct().getName());
                        dto.setModelId(cartItem.getProductModel().getId());
                        dto.setCount(cartItem.getCount());
                        dto.setPrice(cartItem.getProductModel().getPrice());
                        dto.setImgUrl(cartItem.getProduct().getImages().isEmpty() ? null
                                : cartItem.getProduct().getImages().get(0).getImageUrl());
                        dto.setCartItemId(cartItem.getId());
                        // 배송비 계산 (주문 총액이 5만원 이상이면 무료배송)
                        int totalOrderPrice = order.getOrderItems().stream().mapToInt(OrderItem::getTotalPrice).sum();
                        if (totalOrderPrice >= 50000) {
                            dto.setDeliveryFee(0);
                            dto.setDeliveryType("무료배송");
                        } else {
                            dto.setDeliveryFee(3000);
                            dto.setDeliveryType("유료배송");
                        }
                        return dto;
                    }).collect(Collectors.toList());
        }

        return OrderDto.builder()
                .orderNo(order.getId())
                .totalPrice(order.getOrderItems().stream().mapToInt(OrderItem::getTotalPrice).sum())
                .items(orderItemDtos)
                .userName(order.getUsers().getName())
                .orderId(order.getOrderId())
                .email(order.getUsers().getEmail())
                .phone(order.getUsers().getPhone())
                .pCode(order.getUsers().getP_code())
                .loadAddress(order.getUsers().getLoadAddr())
                .lotAddress(order.getUsers().getLotAddr())
                .detailAddress(order.getUsers().getDetailAddr())
                .orderTime(order.getOrderDate().toLocalDate())
                .payInfo(order.getPaymentMethod())
                .totalPrice(order.getTotalPrice())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .finalAmount(order.getFinalAmount())
                .build();
    }


    // 기존에 오더 있는지 확인
    @Transactional(readOnly = true)
    public OrderDto getExistingOrderDto(Users user) {
        List<Order> existingOrders = orderRepository.findByUsersAndOrderStatus(user, OrderStatus.STAY);
        ;

        if (!existingOrders.isEmpty()) {
            Order existingOrder = existingOrders.get(0);
            List<OrderItemDto> orderItemDtos = existingOrder.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
            OrderDto dto = OrderDto.builder()
                    .orderNo(existingOrder.getId())
                    .totalPrice(existingOrder.getOrderItems().stream().mapToInt(OrderItem::getTotalPrice).sum())
                    .items(orderItemDtos)
                    .userName(user.getName())
                    .orderId(existingOrder.getOrderId())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .pCode(user.getP_code())
                    .loadAddress(user.getLoadAddr())
                    .lotAddress(user.getLotAddr())
                    .detailAddress(user.getDetailAddr())
                    .build();

            return dto;
        }
        return null; // 기존 주문이 없으면 null 반환
    }

    // 장바구니에서 주문 생성
    public OrderDto createOrder(String email, List<Long> itemIds, Long couponId) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다"));

        List<Order> existingOrders = orderRepository.findByUsersAndOrderStatus(user, OrderStatus.STAY);
        Order order;

        if (!existingOrders.isEmpty()) {
            order = existingOrders.get(0);
            // 기존 주문 항목 모두 제거
            order.getOrderItems().clear();

            for (int i = 1; i < existingOrders.size(); i++) {
                orderRepository.delete(existingOrders.get(i));
            }
        } else {
            order = Order.createOrder(user, new ArrayList<>());
            order.setOrderStatus(OrderStatus.STAY);
            orderRepository.save(order);
        }

        // 쿠폰 적용
        if (couponId != null) {
            UserCoupon userCoupon = userCouponRepository.findById(couponId)
                    .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

            // 쿠폰이 해당 사용자의 것인지 확인
            if (!userCoupon.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("해당 쿠폰을 사용할 수 없습니다");
            }

            // 사용 여부 체크 및 예외 발생/사용 처리 모두 제거
            order.setUserCoupon(userCoupon);
        } else {
            // 쿠폰이 선택되지 않은 경우 기존 쿠폰 제거
            order.setUserCoupon(null);
        }

        List<OrderItem> newItems = new ArrayList<>();
        int totalAmount = 0;

        for (Long cartItemId : itemIds) {
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new IllegalArgumentException("찾는 아이템이 없습니다"));
            Product product = cartItem.getProduct();
            ProductModel productModel = cartItem.getProductModel();

            OrderItem orderItem = OrderItem.createOrderItems(product, productModel, cartItem.getCount());
            orderItem.setOrder(order);
            newItems.add(orderItem);
            totalAmount += orderItem.getTotalPrice();

            // 기존 주문 항목 중 동일한 제품 및 모델이 있는지 확인
            boolean exists = order.getOrderItems().stream()
                    .anyMatch(item -> item.getProduct().getId().equals(product.getId()) &&
                            item.getProductModel().getId().equals(productModel.getId()));

            if (!exists) {
                // 기존 주문 항목이 없는 경우 새로 추가
                order.addOrderItem(orderItem); // OrderItem 객체를 Order 객체에 추가
                totalAmount += orderItem.getTotalPrice();
            } else {
                // 기존 주문 항목에 있고 수량의 차이가 있다면
                OrderItem existingOrderItem = order.getOrderItems().stream()
                        .filter(item -> item.getProduct().getId().equals(product.getId()) &&
                                item.getProductModel().getId().equals(productModel.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("주문 항목을 찾을 수 없습니다."));

                int newCount = cartItem.getCount();
                int oldCount = existingOrderItem.getCount();
                int difference = newCount - oldCount;

                //재고 수 파악
                if (difference > 0) {
                    existingOrderItem.getProductModel().removeStock(difference);
                } else {
                    existingOrderItem.getProductModel().addStock(Math.abs(difference));
                }

                existingOrderItem.setCount(newCount);
                totalAmount += existingOrderItem.getTotalPrice();
            }
        }

        order.getOrderItems().addAll(newItems);
        orderRepository.save(order);

        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());

        OrderDto dto = OrderDto.builder()
                .orderNo(order.getId())
                .orderId(order.getOrderId())
                .totalPrice(totalAmount)
                .items(orderItemDtos)
                .userName(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .pCode(user.getP_code())
                .loadAddress(user.getLoadAddr())
                .lotAddress(user.getLotAddr())
                .detailAddress(user.getDetailAddr())
                .build();

        return dto;
    }


    // 유효성 검사 - 쿠폰 할인이 적용된 실제 결제 금액으로 검증
    @Transactional(readOnly = true)
    public boolean validatePayment(String orderId, int amount) {
        try {
            // 주문을 조회합니다.
            Order order = orderRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다."));

            // 주문 상세 항목의 가격을 합산합니다.
            int totalPrice = order.getOrderItems().stream()
                    .mapToInt(OrderItem::getTotalPrice)
                    .sum();

            // 배송비 계산 (5만원 미만 시 3,000원)
            int deliveryFee = totalPrice >= 50000 ? 0 : 3000;

            // 쿠폰 할인 계산
            int discountAmount = 0;
            if (order.getUserCoupon() != null) {
                Coupon coupon = order.getUserCoupon().getCoupon();
                if (coupon.getType() == CouponType.AMOUNT) {
                    discountAmount = coupon.getDiscountAmount();
                } else if (coupon.getType() == CouponType.PERCENT) {
                    discountAmount = (int) Math.floor(totalPrice * coupon.getDiscountPercent() / 100.0);
                }
            }

            // 실제 결제 금액 계산
            int expectedAmount = totalPrice + deliveryFee - discountAmount;

            // 결제 금액 확인
            if (expectedAmount == amount) {
                return true;
            } else {
                log.error("결제 금액 불일치: 예상={}, 실제={}", expectedAmount, amount);
                throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
            }
        } catch (IllegalArgumentException e) {
            log.error("validatePayment 에서 발생: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("Unexpected error during payment validation: {}", e.getMessage());
            return false;
        }
    }

    public void updateOrderWithPaymentInfo(String orderId, String paymentMethod, String payInfo) {
        try {
            Order order = orderRepository.findByOrderId(orderId).orElseThrow(() -> new EntityNotFoundException("주문정보를 찾을수 없습니다"));
            order.updatePaymentInfo(paymentMethod, payInfo);
            orderRepository.save(order);

        } catch (EntityNotFoundException e) {
            log.error("updateOrderWithPaymentInfo 에서 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during updating order with payment info: {}", e.getMessage());
            throw new RuntimeException("Updating order with payment info failed", e);
        }
    }

    public void failOrder(String orderId) {
        try {
            // 실패시 해당 주문아이디로 주문을 찾고 상태를 캔슬로 변경 > 재고 다시 원상복구
            Order order = orderRepository.findByOrderId(orderId).orElseThrow(() -> new EntityNotFoundException("주문정보를 찾을수 없습니다."));
            order.setOrderStatus(OrderStatus.CANCEL);

            for (OrderItem orderItem : order.getOrderItems()) {
                orderItem.getProductModel().addStock(orderItem.getOrderPrice());
            }

            orderRepository.save(order);
        } catch (EntityNotFoundException e) {
            log.error("failOrder 에서 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during order cancellation: {}", e.getMessage());
            throw new RuntimeException("Order cancellation failed", e);
        }
    }

    // 바로구매하기 주문 생성
    public OrderDto createOrderByNow(CartDto cartDto, long prId, String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다"));

        int totalAmount = (int) cartDto.getTotalPrice();

        // 기존 주문에 해당 상품이 있는지 확인하고, 있는 경우 기존 주문 삭제
        List<Order> existingOrders = orderRepository.findByUsersAndOrderStatus(user, OrderStatus.STAY);
        Order order;
        if (!existingOrders.isEmpty()) {
            order = existingOrders.get(0);
            order.getOrderItems().clear();
            for (int i = 1; i < existingOrders.size(); i++) {
                orderRepository.delete(existingOrders.get(i));
            }
        } else {
            order = Order.createOrder(user, new ArrayList<>());
            orderRepository.save(order);
        }

        for (CartItemDto cartItemDto : cartDto.getItems()) {
            long modelId = cartItemDto.getModelId();
            ProductModel productModel = productModelRepository.findById(modelId)
                    .orElseThrow(() -> new IllegalArgumentException("찾는 모델이 없습니다"));

            Product product = productRepository.findById(prId)
                    .orElseThrow(() -> new IllegalArgumentException("찾는 제품이 없습니다"));

            // 기존 주문 항목 중 동일한 제품 및 모델이 있는지 확인
            boolean exists = order.getOrderItems().stream()
                    .anyMatch(item -> item.getProduct().getId().equals(product.getId()) &&
                            item.getProductModel().getId().equals(productModel.getId()));

            if (!exists) {
                // 기존 주문 항목이 없는 경우 새로 추가
                OrderItem orderItem = OrderItem.createOrderItems(product, productModel, cartItemDto.getCount());
                order.addOrderItem(orderItem); // OrderItem 객체를 Order 객체에 추가
            } else {
                // 기존 주문 항목에 있고 수량의 차이가 있다면
                OrderItem existingOrderItem = order.getOrderItems().stream()
                        .filter(item -> item.getProduct().getId().equals(product.getId()) &&
                                item.getProductModel().getId().equals(productModel.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("주문 항목을 찾을 수 없습니다."));

                int newCount = cartItemDto.getCount();
                int oldCount = existingOrderItem.getCount();
                int difference = newCount - oldCount;

                if (difference > 0) {
                    existingOrderItem.getProductModel().removeStock(difference);
                } else {
                    existingOrderItem.getProductModel().addStock(Math.abs(difference));
                }

                existingOrderItem.setCount(newCount);
            }
        }

        orderRepository.save(order);

        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
        OrderDto dto = OrderDto.builder()
                .orderNo(order.getId())
                .orderId(order.getOrderId())
                .totalPrice(totalAmount)
                .items(orderItemDtos)
                .userName(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .pCode(user.getP_code())
                .loadAddress(user.getLoadAddr())
                .lotAddress(user.getLotAddr())
                .detailAddress(user.getDetailAddr())
                .build();

        return dto;
    }


    @Transactional(readOnly = true)
    public Page<OrderDto> orderPage(List<OrderDto> orderDtos, int page) {
        Pageable pageable = PageRequest.of(page, 5); // 한 페이지에 표시할 항목 수 5로 고정

        // OrderDto 리스트를 펼쳐서 개별 OrderItemDto로 변환
        List<OrderDto> allItems = orderDtos.stream()
                .flatMap(orderDto -> orderDto.getItems().stream().map(item -> {
                    return OrderDto.builder()
                            .orderNo(orderDto.getOrderNo())
                            .orderTime(orderDto.getOrderTime())
                            .orderId(orderDto.getOrderId())
                            .items(List.of(item))
                            .build();
                }))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allItems.size());

        List<OrderDto> subList = allItems.subList(start, end);
        return new PageImpl<>(subList, pageable, allItems.size());
    }

    // 쿠폰을 주문에 적용 (쿠폰 사용 처리 X, 결제 시점에만 처리)
    @Transactional
    public void applyCouponToOrder(String orderId, Long couponId, String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다"));

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        UserCoupon userCoupon = userCouponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        // 쿠폰이 해당 사용자의 것인지 확인
        if (!userCoupon.getUser().getId().equals(user.getId()))
            throw new IllegalArgumentException("해당 쿠폰을 사용할 수 없습니다");

        // 주문에 이미 같은 쿠폰이 연결되어 있으면 그냥 리턴
        if (order.getUserCoupon() != null && order.getUserCoupon().getId().equals(couponId)) {
            return;
        }
        order.setUserCoupon(userCoupon);
        orderRepository.save(order);
    }

    // 쿠폰 할인이 적용된 실제 결제 금액 계산
    @Transactional
    public int getFinalPaymentAmount(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다"));

        Order order = orderRepository.findByUsersAndOrderStatus(user, OrderStatus.STAY)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        int totalPrice = order.getOrderItems().stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();

        // 배송비 계산 (5만원 미만 시 3,000원)
        int deliveryFee = totalPrice >= 50000 ? 0 : 3000;

        // 쿠폰 할인 계산
        int discountAmount = 0;
        if (order.getUserCoupon() != null) {
            Coupon coupon = order.getUserCoupon().getCoupon();
            if (coupon.getType() == CouponType.AMOUNT) {
                discountAmount = coupon.getDiscountAmount();
            } else if (coupon.getType() == CouponType.PERCENT) {
                discountAmount = (int) Math.floor(totalPrice * coupon.getDiscountPercent() / 100.0);
            }
        }

        int finalAmount = totalPrice + deliveryFee - discountAmount;

        // 저장
        order.setTotalPrice(totalPrice);
        order.setDiscountAmount(discountAmount);
        order.setDeliveryFee(deliveryFee);
        order.setFinalAmount(finalAmount);
        orderRepository.save(order); // 변경 감지로 자동 저장도 가능

        return finalAmount;
    }

    @Transactional
    public void markCouponUsedForOrderByEmail(String email) {
        Users user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;
        Order order = orderRepository.findByUsersAndOrderStatus(user, OrderStatus.STAY)
                .stream().findFirst().orElse(null);
        if (order == null) return;
        if (order.getUserCoupon() == null) return;
        if (order.getUserCoupon().isUsed()) return;
        order.getUserCoupon().setUsed(true);
        userCouponRepository.save(order.getUserCoupon());
    }


    @Transactional
    public void markCouponUsedForOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElse(null);
        if (order == null) return;
        if (order.getUserCoupon() == null) return;
        if (order.getUserCoupon().isUsed()) return;
        order.getUserCoupon().setUsed(true);
        userCouponRepository.save(order.getUserCoupon());
    }


    public List<OrderDto> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("{}", orderRepository.findAllByOrderDateBetween(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()).size());
        List<Order> orders = orderRepository.findAllByOrderDateBetween(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        List<OrderDto> orderDtos = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());

            OrderDto dto = OrderDto.builder()
                    .orderNo(order.getId())
                    .totalPrice(order.getOrderItems().stream().mapToInt(OrderItem::getTotalPrice).sum())
                    .items(orderItemDtos)
                    .userName(order.getUsers().getName())
                    .orderId(order.getOrderId())
                    .email(order.getUsers().getEmail())
                    .phone(order.getUsers().getPhone())
                    .pCode(order.getUsers().getP_code())
                    .loadAddress(order.getUsers().getLoadAddr())
                    .lotAddress(order.getUsers().getLotAddr())
                    .detailAddress(order.getUsers().getDetailAddr())
                    .orderTime(order.getOrderDate().toLocalDate())
                    .build();

            orderDtos.add(dto);
        }
        return orderDtos;
    }


    // 결재 성공 내역
    @Transactional(readOnly = true)
    public List<OrderItem> history(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        List<Order> orders = orderRepository.findByUsersAndOrderStatusOrderByOrderDateDesc(user, OrderStatus.ORDER);

        return orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.toList());
    }

    public List<Order> getAllOrdersForAdmin() {
        return orderRepository.findAllWithDetailsForAdmin();
    }

    public Page<Order> getPagedOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return orderRepository.findAll(pageable);
    }

    public List<DailyOrderCountDto> getRecent7DaysOrderCount() {
        List<Object[]> result = orderRepository.getOrderCountLast7Days();
        return result.stream()
                .map(row -> new DailyOrderCountDto(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        (Long) row[1]
                )).toList();
    }

    public List<CategoryMonthSalesDto> getAllCategorySalesByMonth() {
        List<Object[]> rows = orderRepository.getTotalCategorySales();
        return rows.stream()
                .map(row -> new CategoryMonthSalesDto(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).intValue()
                )).toList();
    }

    public List<CategoryMonthSalesDto> getThisMonthCategorySales() {
        List<Object[]> rows = orderRepository.getThisMonthCategorySales();
        return rows.stream()
                .map(row -> new CategoryMonthSalesDto(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).intValue()
                )).toList();
    }

    public List<Order> findOrdersByUserEmail(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return orderRepository.findByUsersAndOrderStatusOrderByOrderDateDesc(user, OrderStatus.ORDER);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderIdForDetail(String orderId) {
        // 주문 엔티티 조회 (주문 + 주문아이템 + 상품 + 상품모델)
        Order order = orderRepository.findWithDetailsByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));

        // 주문 아이템 → DTO 변환
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(oi -> {
                    OrderItemDto dto = new OrderItemDto();
                    dto.setOrderItemId(oi.getId());
                    dto.setProductId(oi.getProduct().getId());
                    dto.setProductName(oi.getProduct().getName());
                    dto.setModelId(oi.getProductModel().getId());
                    dto.setCount(oi.getCount());
                    dto.setPrice(oi.getProductModel().getPrice());
                    dto.setImgUrl(
                            oi.getProduct().getImages().isEmpty() ?
                                    null : oi.getProduct().getImages().get(0).getImageUrl()
                    );
                    // 배송비 계산
                    int totalOrderPrice = order.getOrderItems().stream().mapToInt(OrderItem::getTotalPrice).sum();
                    if (totalOrderPrice >= 50000) {
                        dto.setDeliveryFee(0);
                        dto.setDeliveryType("무료배송");
                    } else {
                        dto.setDeliveryFee(3000);
                        dto.setDeliveryType("유료배송");
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // DTO 생성 및 리턴
        return OrderDto.builder()
                .orderNo(order.getId())
                .totalPrice(order.getOrderItems().stream().mapToInt(OrderItem::getTotalPrice).sum())
                .items(orderItemDtos)
                .userName(order.getUsers().getName())
                .orderId(order.getOrderId())
                .email(order.getUsers().getEmail())
                .phone(order.getUsers().getPhone())
                .pCode(order.getUsers().getP_code())
                .loadAddress(order.getUsers().getLoadAddr())
                .lotAddress(order.getUsers().getLotAddr())
                .detailAddress(order.getUsers().getDetailAddr())
                .orderTime(order.getOrderDate().toLocalDate())
                .payInfo(order.getPaymentMethod())
                .totalPrice(order.getTotalPrice())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .finalAmount(order.getFinalAmount())
                .build();
    }
}