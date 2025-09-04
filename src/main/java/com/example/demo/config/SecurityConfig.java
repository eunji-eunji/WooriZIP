package com.example.demo.config;

import com.example.demo.oauth2.CustomOAuth2UserService;
import com.example.demo.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        // 1. 기본 OAuth2 요청 리졸버 생성
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        // 2. 모든 로그인 요청에 prompt=select_account 추가하는 커스텀 리졸버
        OAuth2AuthorizationRequestResolver customResolver = new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request);
                if (originalRequest == null) return null;

                Map<String, Object> additionalParams = new HashMap<>(originalRequest.getAdditionalParameters());
                additionalParams.put("prompt", "select_account");

                return OAuth2AuthorizationRequest.from(originalRequest)
                        .additionalParameters(additionalParams)
                        .build();
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request, clientRegistrationId);
                if (originalRequest == null) return null;

                Map<String, Object> additionalParams = new HashMap<>(originalRequest.getAdditionalParameters());
                additionalParams.put("prompt", "select_account");

                return OAuth2AuthorizationRequest.from(originalRequest)
                        .additionalParameters(additionalParams)
                        .build();
            }
        };

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .loginProcessingUrl("/user/login")
                        .successHandler(successHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/user/login")
                        .failureUrl("/user/login?error")
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(customResolver) // ✅ 커스텀 리졸버 적용
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(successHandler())
                );

        return http.build();
    }

    // 사용자 인증 관리 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Dao 기반 사용자 인증 공급자 등록
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // 비밀번호 암호화 전략
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String redirectUrl = (String) session.getAttribute("prevPage");
                if (redirectUrl != null) {
                    session.removeAttribute("prevPage");
                    response.sendRedirect(redirectUrl);
                    return;
                }
            }

            // SavedRequest 있는 경우 or 기본으로 설정된 페이지로 이동
            new SavedRequestAwareAuthenticationSuccessHandler().onAuthenticationSuccess(request, response, authentication);
        };
    }

}
