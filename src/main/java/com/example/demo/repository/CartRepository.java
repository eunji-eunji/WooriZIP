package com.example.demo.repository;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUser(Users user);
}
