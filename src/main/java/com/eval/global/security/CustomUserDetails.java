package com.eval.global.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.eval.domain.employee.dto.EmployeeDTO;

public class CustomUserDetails implements UserDetails {

    private String empNo;
    private String password;
    private String role;
    private String position; // DTO에는 없어도 OK
    private boolean disabled;

    public CustomUserDetails(EmployeeDTO employee) {
        this.empNo = employee.getEmpNo();
        this.password = employee.getPassword();
        this.role = employee.getRole();

        // EmployeeDTO에 position이 없으므로, 조건에 따라 기본값 설정
        // 예: empNo가 특정 범위면 부서장, 아니면 부서원 등
        this.position = employee.getPosition(); 
        // 필요하면 DB나 서비스에서 따로 가져와 덮어쓸 수도 있음

        this.disabled = "퇴사".equals(employee.getStatus());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
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
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return !disabled; }

    public String getRole() { return role; }
    public String getPosition() { return position; }

    public boolean isDeptHead() { return "부서장".equalsIgnoreCase(position); }
    public boolean isStaff() { return "부서원".equalsIgnoreCase(position); }
}