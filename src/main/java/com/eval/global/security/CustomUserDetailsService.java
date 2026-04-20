package com.eval.global.security;

import com.eval.domain.employee.dto.EmployeeDTO;
import com.eval.domain.employee.mapper.EmployeeMapper;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeMapper employeeMapper;

    public CustomUserDetailsService(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String empNo) throws UsernameNotFoundException {
        // 1. MyBatis Mapper를 통해 사원 정보 조회
        EmployeeDTO employee = employeeMapper.findByEmpNo(empNo);
        
        if (employee == null) {
            throw new UsernameNotFoundException("존재하지 않는 사번입니다.");
        }

        // 2. 퇴사자 처리 (예: Status가 'RESIGNED' 또는 '퇴사'인 경우)
        if ("퇴사".equals(employee.getStatus())) {
            throw new DisabledException("퇴사 처리된 계정입니다. 로그인이 제한됩니다.");
        }

        // 3. 5회 오류 잠금 처리
        if (employee.getFailCount() >= 5 || employee.isLocked()) {
            throw new LockedException("비밀번호 5회 오류로 계정이 잠겼습니다. 관리자에게 문의하세요.");
        }

        // 4. Spring Security가 알아들을 수 있는 User 객체로 변환하여 반환
        return User.builder()
                .username(employee.getEmpNo())
                .password(employee.getPassword())
                .roles(employee.getRole()) // 예: "USER", "ADMIN", "LEADER"
                .build();
    }
}