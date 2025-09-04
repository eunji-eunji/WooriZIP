package com.example.demo.controller;

import com.example.demo.dto.InteriorPostDto;
import com.example.demo.dto.QnaPostDto;
import com.example.demo.dto.ReviewPostDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.Product;
import com.example.demo.entity.Users;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;
    private final ProductService productService;
    private final InteriorPostService interiorPostService;
    private final QnaPostService qnaPostService;
    private final ReviewPostService reviewPostService;

    @GetMapping("/signup")
    public String singupForm() {
        return "/user/signUp";
    }

    @PostMapping("/signup")
    public String signUp(@ModelAttribute UserDto dto){
        userService.signUp(dto);
        return "redirect:/";
    }

    @GetMapping("/mypage")
    public String myPage(Authentication authentication, Model model) {
        Users loginUser = UserUtils.getUser(authentication);

        if (loginUser != null) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("userCoupons", userService.getUserCoupons(loginUser));

            // 내가 작성한 게시글 목록
            List<InteriorPostDto> interiorPosts = interiorPostService.findByUser(loginUser);
            List<QnaPostDto> qnaPosts = qnaPostService.findByUser(loginUser);
            List<ReviewPostDto> reviewPosts = reviewPostService.findByUser(loginUser);

            model.addAttribute("myInteriorPosts", interiorPosts);
            model.addAttribute("myQnaPosts", qnaPosts);
            model.addAttribute("myReviews", reviewPosts);
        }

        List<Product> productList = productService.findByUser(loginUser.getId());
        model.addAttribute("products", productList);

        return "/user/mypage";
    }


    @GetMapping("/edit")
    public String editForm(Authentication authentication, Model model) {
        Users loginUser = UserUtils.getUser(authentication);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("isSocial", loginUser.getSocial() != null);

        return "/user/edit";
    }

    @PostMapping("/edit")
    public String editInfo(@ModelAttribute UserDto dto,
                           Authentication authentication) {
        Users loginUser = UserUtils.getUser(authentication);
        userService.edit(dto, loginUser.getId());
        Users updatedUser = userService.findById(loginUser.getId()); // 최신 정보 다시 불러오기

        // Authentication 객체 갱신
        CustomUserDetails userDetails = new CustomUserDetails(updatedUser);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userDetails, authentication.getCredentials(), userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return "redirect:/user/mypage";
    }

    @PostMapping("/delete")
    public String delete(Authentication authentication) {
        Users loginUser = UserUtils.getUser(authentication);
        userService.delete(loginUser.getId());
        return "redirect:/logout";
    }

    @GetMapping("/checkEmail")
    @ResponseBody
    public Map<String, Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return Map.of("exists", exists);
    }

    @GetMapping("/findId")
    public String findIdForm() {
        return "/user/findID";
    }

    @PostMapping("/findId")
    public String findUserId(@RequestParam String name, @RequestParam String phone, Model model) {
        Optional<Users> findUser = userService.findByNameAndPhone(name, phone);
        if (findUser.isPresent()) {
            model.addAttribute("email", findUser.get().getEmail());
        } else {
            model.addAttribute("error", "일치하는 회원 정보가 없습니다.");
        }
        return "user/findID";
    }

    @GetMapping("/findPw")
    public String findPwForm() {
        return "/user/findPW";
    }

    @PostMapping("/findPw")
    public String findUserPw(@RequestParam String email, @RequestParam String phone, RedirectAttributes redirectAttributes) {
        Optional<Users> findUser = userService.findByEmailAndPhone(email, phone);

        if (findUser.isPresent()) {
            String token = passwordResetService.createToken(email);
            emailService.sendResetPasswordLink(email, token);
            redirectAttributes.addFlashAttribute("resetMailSent", "입력하신 이메일로 비밀번호 재설정 링크를 전송했습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "일치하는 회원 정보가 없습니다.");
        }

        return "redirect:/user/findPw";
    }


    @GetMapping("/resetPw")
    public String showResetPwForm(@RequestParam("token") String token, Model model) {
        PasswordResetToken validToken = passwordResetService.validateToken(token);

        if (validToken == null) {
            model.addAttribute("error", "유효하지 않거나 만료된 토큰입니다.");
            return "user/resetPwError"; // 에러 페이지 또는 모달 처리
        }

        model.addAttribute("token", token);
        return "user/resetPw";
    }

    @PostMapping("/resetPw")
    public String resetPassword(@RequestParam String token, @RequestParam String newPw, @RequestParam String confirmPw, Model model) {

        PasswordResetToken resetToken = passwordResetService.validateToken(token);

        if (resetToken == null) {
            model.addAttribute("error", "유효하지 않거나 만료된 토큰입니다.");
            return "user/resetPwError";
        }

        if (!newPw.equals(confirmPw)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "user/resetPw";
        }

        userService.updatePassword(resetToken.getEmail(), newPw);

        passwordResetService.markTokenUsed(resetToken);

        return "redirect:/user/login?resetSuccess";
    }
}