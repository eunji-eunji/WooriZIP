package com.example.demo.controller;

import com.example.demo.dto.QnaPostDto;
import com.example.demo.entity.Users;
import com.example.demo.service.QnaPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Controller
@RequiredArgsConstructor
@RequestMapping("/qna")
public class QnaPostController {

    private final QnaPostService qnaPostService;

    // QnA 등록 (파일 업로드 포함)
    @PostMapping("/create")
    public String create(@RequestParam("productId") Long productId,
                         @ModelAttribute QnaPostDto dto,
                         @RequestParam(value = "files", required = false) MultipartFile[] files,
                         @RequestParam(value = "isSecret", required = false) String isSecret,
                         Authentication authentication) throws IOException {

        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        Users user = UserUtils.getUser(authentication);
        dto.setProductId(productId);
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        
        // 비밀글 설정
        dto.setSecret("true".equals(isSecret));

        if (files != null && files.length > 0 && !files[0].isEmpty()) {
            dto.setFiles(Arrays.asList(files));
        }

        Long qnaId = qnaPostService.saveQna(dto);
        return String.format("redirect:/products/%d?activeTab=qna&scrollTo=%d#qna-section", productId, qnaId);
    }

    // QnA 수정
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute QnaPostDto dto,
                         @RequestParam(value = "files", required = false) MultipartFile[] files,
                         @RequestParam(value = "isSecret", required = false) String isSecret,
                         @RequestParam(required = false) Integer qnaPage,
                         @RequestParam(required = false) String qnaFilter,
                         @RequestParam(required = false) Boolean fromMyPage,
                         Authentication authentication) throws IOException {

        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        Users user = UserUtils.getUser(authentication);
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        
        // 비밀글 설정
        dto.setSecret("true".equals(isSecret));

        if (files != null && files.length > 0 && !files[0].isEmpty()) {
            dto.setFiles(Arrays.asList(files));
        }

        qnaPostService.updateQna(id, dto);

        if (Boolean.TRUE.equals(fromMyPage)) {
            return "redirect:/user/mypage";
        }

        return String.format("redirect:/products/%d?activeTab=qna&scrollTo=%d#qna-section", dto.getProductId(), id);
    }

    // QnA 삭제
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam Long productId,
                         @RequestParam(required = false) Boolean fromMyPage,
                         Authentication authentication) {
        String email = UserUtils.getEmail(authentication);
        if (email == null) return "redirect:/user/login";
        Users user = UserUtils.getUser(authentication);
        qnaPostService.deleteQna(id, user.getEmail());

        if (Boolean.TRUE.equals(fromMyPage)) {
            return "redirect:/user/mypage";
        }
        return String.format("redirect:/products/%d?activeTab=qna", productId);
    }
}
