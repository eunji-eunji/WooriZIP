package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class UserDto {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "로그인시 사용할 이메일을 입력해주세요.")
    @Email(message = "이메일 형식으로 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Length(min = 3, max = 16, message = "6~16자리 비밀번호를 입력해주세요.")
    private String userPw;

    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phone;

    @NotBlank(message = "페이지에서 사용할 닉네임을 입력해주세요.")
    private String nickname;

    private String gender;

    @NotNull(message = "생일에 맞춰 쿠폰이 발급됩니다.")
    @Past(message = "오늘 이전 날짜를 입력해주세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birth;

    @NotBlank(message = "주소는 필수 입력 값입니다.")
    private String p_code;

    @NotBlank(message = "주소는 필수 입력 값입니다.")
    private String loadAddr;

    @NotBlank(message = "주소는 필수 입력 값입니다.")
    private String lotAddr;

    @NotBlank(message = "주소는 필수 입력 값입니다.")
    private String detailAddr;

    private String extraAddr;

    private int residenceType;
}
