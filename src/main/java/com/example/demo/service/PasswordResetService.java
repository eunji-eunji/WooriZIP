package com.example.demo.service;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;

    public String createToken(String email) {
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // 유효시간 10분
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        return token;
    }

    public PasswordResetToken validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> (PasswordResetToken) t)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    public void markTokenUsed(PasswordResetToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }
}
