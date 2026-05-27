package com.eval.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.eval.global.security.CustomAuthFailureHandler;
import com.eval.global.security.CustomAuthSuccessHandler;
import com.eval.global.security.CustomUserDetails;
import com.eval.global.security.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthFailureHandler customAuthFailureHandler;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/department/**").hasRole("ADMIN")

                // 임원(level_id=6) 또는 ADMIN만 접근 가능 — URL 하이픈 포함
                .requestMatchers("/evaluation/final-grade/**").access((authentication, request) -> {
                    var principal = authentication.get().getPrincipal();
                    if (principal instanceof CustomUserDetails userDetails) {
                        return new AuthorizationDecision(
                            userDetails.isExecutive() || "ADMIN".equals(userDetails.getRole())
                        );
                    }
                    return new AuthorizationDecision(false);
                })

                // 평가 점수/등급 현황: 부서장, 임원, ADMIN만
                .requestMatchers("/evaluation/employee-score/**").access((authentication, request) -> {
                    var principal = authentication.get().getPrincipal();
                    if (principal instanceof CustomUserDetails userDetails) {
                        return new AuthorizationDecision(
                            userDetails.isDeptHead() || userDetails.isExecutive() || "ADMIN".equals(userDetails.getRole())
                        );
                    }
                    return new AuthorizationDecision(false);
                })

                .requestMatchers("/evaluation/performance/**", "/evaluation/competency/**",
                                 "/evaluation/multi/**", "/interview/**").authenticated()
                .requestMatchers("/evaluation/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("empNo")
                .passwordParameter("password")
                .successHandler(customAuthSuccessHandler)
                .failureHandler(customAuthFailureHandler)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            .rememberMe(remember -> remember
                .key("evalProjectKey")
                .tokenValiditySeconds(60 * 60 * 24 * 7)
                .userDetailsService(customUserDetailsService)
                .rememberMeParameter("remember-me")
            )

            .sessionManagement(session -> session
                .sessionFixation().changeSessionId()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired=true")
            )

            .csrf(csrf -> csrf.disable()
                .exceptionHandling(ex -> ex
                    .accessDeniedPage("/access-denied")));

        return http.build();
    }
}