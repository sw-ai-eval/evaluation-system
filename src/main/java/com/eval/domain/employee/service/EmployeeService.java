package com.eval.domain.employee.service;

import com.eval.domain.employee.dto.EmployeeDTO;
import com.eval.domain.employee.mapper.EmployeeMapper;
import org.springframework.security.crypto.password.PasswordEncoder; // 🌟 암호화 도구 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder; // 암호화 도구

    public EmployeeService(EmployeeMapper employeeMapper, PasswordEncoder passwordEncoder) {
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 🌟 새 사원 등록 (비밀번호 자동 암호화 포함)
     */
    @Transactional
    public void registerEmployee(EmployeeDTO dto) {
        // 사용자가 입력한 평문 비밀번호(예: "1234")를 암호화($2a$10$...)
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        
        // 암호화된 비밀번호로 다시 설정
        dto.setPassword(encodedPassword);
        
        // DB에 저장
        // employeeMapper.insertEmployee(dto); // 이 메서드가 mapper에 정의되어 있어야 함
    }

    @Transactional
    public int increaseFailCount(String empNo) {
        employeeMapper.incrementFailCount(empNo);
        return employeeMapper.getFailCount(empNo);
    }
    
    @Transactional
    public void resetFailCount(String empNo) {
        employeeMapper.resetFailCount(empNo);
    }
}