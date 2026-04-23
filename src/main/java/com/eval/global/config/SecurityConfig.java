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
    public org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/department/**").hasRole("ADMIN")
                .anyRequest().authenticated()
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
            // 3. 로그아웃 설정 (세션 및 쿠키 삭제 보강)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true) // 서버 세션 삭제
                .deleteCookies("JSESSIONID") // 클라이언트 쿠키 삭제
                .permitAll()
            )
            // 4. 세션 관리 설정
            .sessionManagement(session -> session
                // 세션 고정 보호: 로그인 시마다 세션 ID를 새로 발급하여 가로채기 방지
                .sessionFixation().changeSessionId()
                
                // 동시 세션 제어: 한 계정당 허용 세션 수 제한
                .maximumSessions(1) 
                // false: 나중에 로그인한 사람이 기존 세션을 밀어냄 (사용자 편의성)
                // true: 먼저 로그인한 사람이 있을 경우 새 로그인 차단 (보안 중시)
                .maxSessionsPreventsLogin(false) 
                // 세션이 만료(중복 로그인 등)되었을 때 이동할 페이지
                .expiredUrl("/login?expired=true") 
            )
            // 5. CSRF 설정
            .csrf(csrf -> csrf.disable()
            
            // 권한 없이 접근할 경우
            .exceptionHandling(ex -> ex
          		 .accessDeniedPage("/access-denied")));
        	
        return http.build();
    }

}