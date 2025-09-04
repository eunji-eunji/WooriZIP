package com.example.demo.repository;

import com.example.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Users> findByNameAndPhone(String name, String phone);

    Optional<Users> findByEmailAndPhone(String email, String phone);
}
