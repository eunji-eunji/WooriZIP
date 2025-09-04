package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/policy")
public class PolicyController {

    @GetMapping("/privacy")
    public String privacy() {
        return "policy/privacy";
    }

    @GetMapping("/brand")
    public String brand() {
        return "policy/brand";
    }

    @GetMapping("/customer-service")
    public String customerService() {
        return "policy/customer-service";
    }

    @GetMapping("/email-rejection")
    public String emailRejection() {
        return "policy/email-rejection";
    }
} 