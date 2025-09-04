package com.example.demo.controller;

import com.example.demo.dto.ChatbotResponseDto;
import com.example.demo.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @GetMapping("/chatbot")
    public String chatbotPage(Model model, Authentication authentication) {
        String email = com.example.demo.controller.UserUtils.getEmail(authentication);
        model.addAttribute("loginUser", email != null ? (com.example.demo.entity.Users) com.example.demo.controller.UserUtils.getUser(authentication) : null);
        return "chatbot/chatbot";
    }

    @PostMapping("/api/chatbot/chat")
    @ResponseBody
    public ResponseEntity<ChatbotResponseDto> chat(@RequestParam String message) {
        ChatbotResponseDto response = chatbotService.processMessage(message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/chatbot/suggestions")
    @ResponseBody
    public ResponseEntity<List<String>> getSuggestions() {
        List<String> suggestions = chatbotService.getCommonSuggestions();
        return ResponseEntity.ok(suggestions);
    }
} 