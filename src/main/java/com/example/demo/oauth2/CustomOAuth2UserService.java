package com.example.demo.oauth2;

import com.example.demo.constant.Role;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;


    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);

        //어떤 플랫폼인지
        String registrationId = request.getClientRegistration().getRegistrationId(); // google, kakao, naver
        String userNameAttribute = request.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 플랫폼별 사용자 정보 파싱
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttribute, oAuth2User.getAttributes());
        //이메일로 기존 회원 조회
        Users user = userRepository.findByEmail(attributes.getEmail())
                .orElseGet(() -> saveUser(attributes)); // 없으면 저장
        //Security가 사용하도록 OAuth2User 구현체 반환
        return new CustomOAuth2User(user, attributes.getAttributes());
    }

    private Users saveUser(OAuthAttributes attr) {
        Users user = Users.builder()
                .name(attr.getName())
                .email(attr.getEmail())
                .nickname(attr.getName())
                .userPw("")
                .phone("")
                .gender("")
                .birth(LocalDate.now()) //생일 정보 수정 권유 팝업 띄우기
                .p_code("")
                .loadAddr("")
                .lotAddr("")
                .detailAddr("")
                .extraAddr("")
                .residenceType(0)
                .social(attr.getProvider())
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }
}
