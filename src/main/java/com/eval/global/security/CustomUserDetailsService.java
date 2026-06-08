package com.eval.global.security;

import com.eval.domain.employee.dto.EmployeeDTO;
import com.eval.domain.employee.mapper.EmployeeMapper;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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
        EmployeeDTO employee = employeeMapper.findByEmpNo(empNo);
        
        // 1. 사번이 아예 없는 경우
        if (employee == null) {
            throw new InternalAuthenticationServiceException("존재하지 않는 사번입니다.");
        }

        // 2. 상태값 판별
        boolean isResigned = employee.getStatus() != null &&
                ("퇴사".equals(employee.getStatus().trim()) || "RESIGNED".equals(employee.getStatus().trim()));
        boolean isAccountLocked = employee.getFailCount() >= 5 || employee.getLocked() == 1;

        if (isResigned) {
            throw new DisabledException("퇴사 처리된 계정입니다.");
        }
        if (isAccountLocked) {
            throw new LockedException("계정이 잠겼습니다.");
        }

        return new CustomUserDetails(employee);
    }
}