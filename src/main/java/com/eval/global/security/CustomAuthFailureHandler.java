package com.eval.global.security;

import com.eval.domain.employee.service.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

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
        String errorMessage = "사번 또는 비밀번호가 맞지 않습니다.";

        if (exception instanceof BadCredentialsException) {
            // 비밀번호가 틀렸을 때 -> 실패 횟수 증가 로직 호출
            int failCount = employeeService.increaseFailCount(empNo);
            
            if (failCount >= 5) {
                errorMessage = "비밀번호 5회 오류로 계정이 잠겼습니다. 관리자에게 문의하세요.";
            } else {
                errorMessage = "비밀번호를 잘못 입력했습니다. (남은 기회: " + (5 - failCount) + "회)";
            }
        } else if (exception instanceof LockedException) {
            errorMessage = exception.getMessage();
        } else if (exception instanceof DisabledException) {
            errorMessage = exception.getMessage();
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않는 사번입니다.";
        }

        // 한글 깨짐 방지를 위한 URL 인코딩
        errorMessage = URLEncoder.encode(errorMessage, "UTF-8");
        setDefaultFailureUrl("/login?error=true&errorMessage=" + errorMessage);
        
        super.onAuthenticationFailure(request, response, exception);
    }
}