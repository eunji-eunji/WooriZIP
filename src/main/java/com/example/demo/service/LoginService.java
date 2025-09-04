package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.Users;
import com.example.demo.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final LoginRepository loginRepository;
    private final BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    public Users login(UserDto dto) {
        return loginRepository.findByEmail(dto.getEmail())
                .filter(m -> passwordEncoder.matches(dto.getUserPw(), m.getUserPw()))
                .orElse(null);
    }
}