package com.example.demo.controller;

import com.example.demo.entity.Users;
import com.example.demo.oauth2.CustomOAuth2User;
import com.example.demo.security.CustomUserDetails;
import org.springframework.security.core.Authentication;

public class UserUtils {
    public static String getEmail(Authentication authentication) {
        if (authentication == null) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getEmail();
        } else if (principal instanceof CustomOAuth2User oauth2User) {
            return oauth2User.getUser().getEmail();
        }
        return null;
    }

    public static Users getUser(Authentication authentication) {
        if (authentication == null) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        } else if (principal instanceof CustomOAuth2User oauth2User) {
            return oauth2User.getUser();
        }
        return null;
    }
}
