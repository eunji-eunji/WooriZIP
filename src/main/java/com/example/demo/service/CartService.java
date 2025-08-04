package com.example.demo.service;

import com.example.demo.dto.CartDto;
import com.example.demo.dto.CartItemDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartDto getCartByEmail(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을수 없습니다"));
        Cart cart = cartRepository.findByUser(user);
        return cart != null ? new CartDto(cart) : null;
    }

    public void addItemToCart(CartDto cartDto, String email, Long productId) {
        // 1. 유저 및 상품 확인
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("제품을 찾을 수 없습니다"));

        // 2. 카트 확인 또는 생성
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = Cart.createCart(user);
            cartRepository.save(cart);
        }

        // 4. 각 아이템 처리
        for (CartItemDto itemDto : cartDto.getItems()) {

            ProductModel productModel = product.getProductModels().stream()
                    .filter(findModel -> findModel.getId().equals(itemDto.getModelId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당 상품 옵션을 찾을 수 없습니다."));

            // 기존 아이템 여부 확인
            CartItem findItem = cart.getCartItems().stream()
                    .filter(cartItem -> cartItem.getProduct().equals(product)
                            && cartItem.getProductModel().equals(productModel))
                    .findFirst()
                    .orElse(null);

            if (findItem != null) {
                findItem.addCount(itemDto.getCount());
            } else {
                CartItem cartItem = CartItem.createCartItem(product, productModel, itemDto.getCount(), cart);
                cart.addCartItems(cartItem);

                // insert 유도
                cartItemRepository.save(cartItem);
                cartItemRepository.flush();
            }
        }

        // 카트 저장
        cartRepository.save(cart);
        cartRepository.flush();

    }

    // 전체 삭제 메소드
    public void clearCart(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을수 없습니다"));
        Cart cart = cartRepository.findByUser(user);
        if (cart != null) {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }else {
            throw new IllegalArgumentException("장바구니가 없습니다");
        }
    }

    // 장바구니에서 개별 삭제
    public void removeItemFromCart(String email, Long cartItemId) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다"));
        Cart cart = cartRepository.findByUser(user);
        // 장바구니가 비어있지 않으면
        if (cart != null) {
            CartItem removeCartItem = null; // 삭제하려는 아이템을 담을 변수
            for (CartItem cartItem : cart.getCartItems()) { // cart 안에 아이템을 순회하면서 찾기
                if (cartItem.getId().equals(cartItemId)) {
                    removeCartItem = cartItem; // 찾으면 빠져나옴
                    break;
                }
            }
            // 예외 처리 삭제하려는 카트아이템이 없다면
            if (removeCartItem == null) {
                throw new EntityNotFoundException("해당 상품을 찾을 수 없습니다");
            }

            cart.removeItems(removeCartItem);
            cartRepository.save(cart);
        }
    }

    // 업데이트
    public void updateCartItemQuantity(Long cartItemId, int count) {
        Optional<CartItem> findCartItem = cartItemRepository.findById(cartItemId);
        if (findCartItem.isPresent()) {
            CartItem cartItem = findCartItem.get();
            cartItem.updateCount(count);
            cartItemRepository.save(cartItem);
        }
    }

    public int getItemStock(Long cartItemId) {
        int stock = 0 ;
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(()->new EntityNotFoundException("찾을 수 없습니다"));
        stock = item.getProductModel().getPrStock();
        return  stock;
    }

    // 선택 삭제
    public void deleteSelectedItems(List<Long> cartItemIds) {
        for (Long id : cartItemIds) {
            cartItemRepository.deleteById(id);
        }
    }

    public void removeItemsFromCart(String email, List<Long> cartItemIds) {
        for (Long id : cartItemIds) {
            removeItemFromCart(email, id);
        }
    }
}
