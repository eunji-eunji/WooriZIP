package com.example.demo.service;

import com.example.demo.entity.Product;
import com.example.demo.entity.Users;
import com.example.demo.entity.Wishlist;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public void toggleWishlist(Users user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        Optional<Wishlist> wish = wishlistRepository.findByUserAndProduct(user, product);

        if (wish.isPresent()) {
            wishlistRepository.delete(wish.get()); // 찜 해제
        } else {
            Wishlist newWish = new Wishlist();
            newWish.setUser(user);
            newWish.setProduct(product);
            wishlistRepository.save(newWish); // 찜 등록
        }
    }

}
