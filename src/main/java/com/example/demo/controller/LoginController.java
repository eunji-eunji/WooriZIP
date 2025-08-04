package com.example.demo.controller;

import com.example.demo.oauth2.CustomOAuth2User;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @GetMapping("/user/login")
    public String loginForm(HttpServletRequest request) {
        // 이전 페이지 URL을 가져옴
        String referrer = request.getHeader("Referer");

        // 세션에 저장 (로그인 페이지가 아닌 경우만)
        if (referrer != null && !referrer.contains("/user/login") && !referrer.contains("/user/signup")) {
            HttpSession session = request.getSession();
            session.setAttribute("prevPage", referrer);
        }
        return "user/login";
    }

    @GetMapping("/user/logout")
    public String logout(){
        return "redirect:/logout";
    }
}