package com.example.demo.oauth2;

import com.example.demo.entity.Users;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User  implements OAuth2User {
    private final Users user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Users user, Map<String, Object> attributes){
        this.user=user;
        this.attributes=attributes;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = (user.getRole() != null) ? user.getRole().name() : "USER";
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }
}
