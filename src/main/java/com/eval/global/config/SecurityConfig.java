package com.eval.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.eval.global.security.CustomAuthFailureHandler;
import com.eval.global.security.CustomAuthSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthFailureHandler customAuthFailureHandler;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 접근 권한 설정 (인가)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll() // 누구나 접근 가능
                .requestMatchers("/admin/**").hasRole("ADMIN")              // ADMIN 권한만 접근 가능
                .anyRequest().authenticated()                               // 나머지는 로그인 필수
            )
            // 2. 로그인 설정
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("empNo")
                .passwordParameter("password")
                .successHandler(customAuthSuccessHandler)
                .failureHandler(customAuthFailureHandler)
                .permitAll()
            )
            // 3. 로그아웃 설정
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true) // 세션 삭제
                .permitAll()
            )
            // 4. CSRF 설정 (임시 비활성화)
            // 비번 초기화(POST 방식)가 작동위함
            // 나중에 바꾸기
            .csrf(csrf -> csrf.disable());
            
        return http.build();
    }

}