package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")  // application.properties에서 불러옴
    private String fromEmail;
    private final String fromName = "WooriZIP";

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordLink(String email, String token) {
        String resetUrl = "http://localhost/user/resetPw?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setFrom(new InternetAddress(fromEmail, fromName));  // 이름 + 이메일
            helper.setSubject("[우리ZIP] 비밀번호 재설정 안내");
            helper.setText("비밀번호를 재설정하려면 아래 링크를 클릭하세요:\n" + resetUrl + "\n\n이 링크는 10분간 유효합니다.", false);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
