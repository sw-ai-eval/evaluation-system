package com.eval.domain.employee.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpManageDTO {

    private String empNo;
    private String password;
    private String name;

    // 🔥 여기 핵심 변경
    private String deptId;
    private String deptName; // 부서명 조인으로 찾을 거임

    private String status;
    private int failCount;
    private int locked;
    private String role; // 권한

    private String email;
    private String phone;
    private String positionLevel; //
    private String job;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate resignDate;
    
    private String position; // leader 여부 (checkbox)
    
    private LocalDateTime createdAt;
    private String createdBy;
}