package com.eval.domain.employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "employee_52")
public class Employee {

    @Id
    @Column(name = "emp_no", length = 50)
    private String empNo;

    @Column(name = "dept_id", nullable = false, length = 50)
    private String deptId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "resign_date")
    private LocalDate resignDate;

    @Column(name = "level_id", nullable = false, length = 50)
    private Long levelId;

    @Column(name = "position", length = 50) // 부서장, 부서원
    private String position;

    @Column(name = "role", nullable = false, length = 20)
    private String role = "USER";

    @Column(name = "job_id", length = 50)
    private Long jobId;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "locked", nullable = false)
    private Integer locked = 0;

    @Column(name = "login_fail_count", nullable = false)
    private int failCount = 0;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;
}