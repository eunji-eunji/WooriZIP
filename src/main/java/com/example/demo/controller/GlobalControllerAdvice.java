package com.example.demo.controller;

import com.example.demo.entity.Users;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// loginUser 모델 전역 처리
@ControllerAdvice
public class GlobalControllerAdvice {
    @ModelAttribute("loginUser")
    public Users getLoginUser(Authentication authentication) {
        return UserUtils.getUser(authentication);
    }
}
