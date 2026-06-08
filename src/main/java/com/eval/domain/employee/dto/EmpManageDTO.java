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

    private String deptId;
    private String deptName;

    private String status;
    private Integer locked;
    private Integer failCount;
    private String role; // 권한

    private String email;
    private String phone;
    private Long jobId;
    private String jobName;
    
    private Long levelId;
    private String levelName; 
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate resignDate;
    
    private String position; 
    
    private LocalDateTime createdAt;
    private String createdBy;
    
    public String getPositionDisplay() {
        return position;
    }
    
    private int overdueResignFlag;// 
    
}