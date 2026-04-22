package com.eval.global.security;

import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.eval.domain.employee.service.EmployeeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final EmployeeService employeeService;

    public CustomAuthFailureHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        String empNo = request.getParameter("empNo");
        
        // 1. 공통으로 사용할 메시지 정의
        String defaultMessage = "사번 또는 비밀번호가 잘못 되었습니다.\n사번과 비밀번호를 정확히 입력해 주세요.";
        String lockedMessage = "비밀번호 5회 오류로 계정이 잠겼습니다.\n관리자에게 문의하세요.";
        
        String errorMessage = defaultMessage; // 일단 기본값으로 시작
        
        // 2. 조건별 분기
        if (exception instanceof BadCredentialsException) {
            // 비번 틀림 -> 카운트 증가
            int failCount = employeeService.increaseFailCount(empNo);
            
            // 5회 이상이면 잠금 메시지, 아니면 남은 기회 안내
            errorMessage = (failCount >= 5) ? lockedMessage : "비밀번호를 잘못 입력했습니다.\n(남은 기회: " + (5 - failCount) + "회)";
                
        } else if (exception instanceof LockedException) {
            // 이미 잠긴 계정일 때
        	employeeService.forceLock(empNo);
            errorMessage = lockedMessage;
            
        } else if (exception instanceof DisabledException) {
            // 퇴사자일 때
            errorMessage = "퇴사 처리된 계정입니다.";
        }

        // 3. 인코딩 후 리다이렉트
        errorMessage = URLEncoder.encode(errorMessage, "UTF-8");
        response.sendRedirect("/login?error=true&errorMessage=" + errorMessage);
    }
}