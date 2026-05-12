package com.eval.global.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.eval.domain.employee.dto.EmpManageDTO;

public class CustomUserDetails implements UserDetails {

    private String empNo;
    private String password;
    private String role;
    private String position; // 부서장/부서원 구분용
    private boolean disabled;

    public CustomUserDetails(EmpManageDTO employee) {
        this.empNo = employee.getEmpNo();
        this.password = employee.getPassword();
        this.role = employee.getRole();
        this.position = employee.getPosition(); // 여기서 position 세팅
        this.disabled = "퇴사".equals(employee.getStatus());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return empNo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !disabled;
    }

    // 추가 getter
    public String getRole() {
        return role;
    }

    public String getPosition() {
        return position;
    }

    // helper 메서드 (부서장/부서원 구분용)
    public boolean isDeptHead() {
        return "부서장".equalsIgnoreCase(position);
    }

    public boolean isStaff() {
        return "부서원".equalsIgnoreCase(position);
    }
}