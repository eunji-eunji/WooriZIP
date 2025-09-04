package com.example.demo.oauth2;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String name;
    private final String email;
    private final String provider;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String name, String email, String provider){
        this.attributes=attributes;
        this.name = name;
        this.email = email;
        this.provider = provider;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttribute, Map<String, Object> attributes){
        if (registrationId.equals("naver")) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return new OAuthAttributes(
                    attributes,
                    (String) response.get("name"),
                    (String) response.get("email"),
                    "naver"
            );
        } else if (registrationId.equals("kakao")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            return new OAuthAttributes(
                    attributes,
                    (String) profile.get("nickname"),
                    (String) kakaoAccount.get("email"),
                    "kakao"
            );
        }

        // google
        return new OAuthAttributes(
                attributes,
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                "google"
        );
    }
}
