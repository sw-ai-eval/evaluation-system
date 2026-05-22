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
    private String position;
    private int levelId;
    private boolean disabled;

    public CustomUserDetails(EmployeeDTO employee) {
        this.empNo = employee.getEmpNo();
        this.password = employee.getPassword();
        this.role = employee.getRole();
        this.position = employee.getPosition();
        this.levelId = employee.getLevelId() != null ? employee.getLevelId() : 0;
        this.disabled = "퇴사".equals(employee.getStatus());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String getPassword()  { return password; }
    @Override public String getUsername()  { return empNo; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return !disabled; }

    public String getRole()     { return role; }
    public String getPosition() { return position; }
    public int getLevelId()     { return levelId; }

    public boolean isDeptHead()   { return "부서장".equalsIgnoreCase(position); }
    public boolean isStaff()      { return "부서원".equalsIgnoreCase(position); }
    public boolean isExecutive()  { return this.levelId == 6; }
}