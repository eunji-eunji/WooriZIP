package com.example.demo.entity;

import com.example.demo.constant.Role;
import com.example.demo.dto.UserDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String userPw;

    private String phone;

    private String nickname;

    private String gender;

    private LocalDate birth;

    @Column(name = "p_code", nullable = false)
    private String p_code;

    @Column(name = "loadAddr", nullable = false)
    private String loadAddr;

    @Column(name = "lotAddr", nullable = false)
    private String lotAddr;

    @Column(name = "detailAddr", nullable = false)
    private String detailAddr;

    @Column(name = "extraAddr")
    private String extraAddr;

    private int residenceType;

    private String social;

    @Enumerated(EnumType.STRING)
    private Role role;


    @Builder
    public Users(String name, String email, String userPw, String phone, String nickname, String gender, LocalDate birth,
                 String p_code, String loadAddr, String lotAddr, String detailAddr, String extraAddr,
                 int residenceType, String social, Role role){

        this.name = name;
        this.email = email;
        this.userPw = userPw;
        this.phone = phone;
        this.nickname = nickname;
        this.gender = gender;
        this.birth = birth;
        this.p_code = p_code;
        this.loadAddr = loadAddr;
        this.lotAddr = lotAddr;
        this.detailAddr = detailAddr;
        this.extraAddr = extraAddr;
        this.residenceType = residenceType;
        this.social = social;
        this.role = role;
    }

    public static Users createUser(UserDto dto, PasswordEncoder passwordEncoder) {
        String encodedPassword = passwordEncoder.encode(dto.getUserPw());
        return Users.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .userPw(encodedPassword)
                .phone(dto.getPhone())
                .nickname(dto.getNickname())
                .gender(dto.getGender())
                .birth(dto.getBirth())
                .p_code(dto.getP_code())
                .loadAddr(dto.getLoadAddr())
                .lotAddr(dto.getLotAddr())
                .detailAddr(dto.getDetailAddr())
                .extraAddr(dto.getExtraAddr())
                .residenceType(dto.getResidenceType())
                .role(Role.USER)
                .build();
    }

    public void updateUser(UserDto dto, PasswordEncoder passwordEncoder) {
        if (dto.getUserPw() != null && !dto.getUserPw().isBlank()) {
            this.userPw = passwordEncoder.encode(dto.getUserPw());
        }

        this.phone = dto.getPhone();
        this.nickname = dto.getNickname();
        this.gender = dto.getGender();
        this.birth = dto.getBirth();
        this.p_code = dto.getP_code();
        this.loadAddr = dto.getLoadAddr();
        this.lotAddr = dto.getLotAddr();
        this.detailAddr = dto.getDetailAddr();
        this.extraAddr = dto.getExtraAddr();
        this.residenceType = dto.getResidenceType();
    }

}