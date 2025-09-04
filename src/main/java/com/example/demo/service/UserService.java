package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Order;
import com.example.demo.entity.UserCoupon;
import com.example.demo.entity.Users;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCouponRepository userCouponRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final QnaPostRepository qnaPostRepository;
    private final ReviewPostRepository reviewPostRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public void signUp(UserDto dto) {
        Users user = Users.createUser(dto, passwordEncoder);
        userRepository.save(user);
    }

    public Users findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Users edit(UserDto dto, Long id) {
        Users user = userRepository.findById(id).orElseThrow();
        user.updateUser(dto, passwordEncoder);  // 핵심 변경
        return user;
    }

    public void delete(Long id) {
        Users user = userRepository.findById(id).get();

        // 사용자가 작성한 QnA 글 삭제
        qnaPostRepository.deleteByEmail(user.getEmail());

        // 사용자가 작성한 리뷰 글 삭제
        reviewPostRepository.deleteByEmail(user.getEmail());

        // 주문 및 주문 항목 삭제
        List<Order> orders = orderRepository.findByUsers(user);
        for (Order order : orders) {
            orderItemRepository.deleteAllByOrder(order);  // 주문 항목 먼저 삭제
        }
        orderRepository.deleteAll(orders);                // 주문 삭제

        // 장바구니 관련 삭제
        Cart cart = cartRepository.findByUser(user);
        if (cart != null) {
            cartItemRepository.deleteAllByCart(cart);     // 장바구니 항목 먼저 삭제
            cartRepository.delete(cart);                  // 장바구니 삭제
        }

        // 유저 삭제
        userRepository.deleteById(id);
    }


    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<Users> findByNameAndPhone(String name, String phone) {
        return userRepository.findByNameAndPhone(name, phone);
    }

    public Optional<Users> findByEmailAndPhone(String email, String phone) {
        return userRepository.findByEmailAndPhone(email, phone);
    }

    public Optional<Users> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void updatePassword(String email, String rawPassword) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String encodedPw = passwordEncoder.encode(rawPassword);
        user.setUserPw(encodedPw);

        userRepository.save(user);
    }

    public List<UserCoupon> getUserCoupons(Users user) {
        return userCouponRepository.findByUserAndUsedFalse(user);
    }

}
