package com.eval.domain.employee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeDTO {
    private String empNo;       // 사번 (예: 1000)
    private String password;    // 비밀번호
    private String empName;     // 성명
    private String deptCode;    // 부서코드
    private String status;      // 재직상태 (재직, 휴직, 퇴사)
    private int failCount;      // 비밀번호 실패 횟수
    private int locked;     	// 계정 잠금 여부
    private String role;        // 권한 (USER, LEADER, ADMIN)
}
