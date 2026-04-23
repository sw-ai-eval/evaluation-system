package com.eval.domain.employee.service;

import com.eval.domain.employee.dto.EmployeeDTO;
import com.eval.domain.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 신규 사원 등록
     */
    @Transactional
    public void registerEmployee(EmployeeDTO dto) {
        // 1. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(encodedPassword);
        
        // 2. DB 저장 실행 (주석 해제 후 Mapper와 연결 필요)
        // employeeMapper.insertEmployee(dto); 
    }

    /**
     * 관리자: 비밀번호 초기화 ('1234') 및 잠금 해제
     */
    @Transactional
    public void resetEmployeePassword(String targetEmpNo) {
        String defaultPassword = "1234";
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        
        // Mapper의 resetPasswordAndUnlock 메서드 호출
        employeeMapper.resetPasswordAndUnlock(targetEmpNo, encodedPassword);
    }

    /**
     *  로그인 실패 횟수 증가 및 자동 잠금 로직
     */
    @Transactional
    public int increaseFailCount(String empNo) {
        employeeMapper.incrementFailCount(empNo);
        int currentFailCount = employeeMapper.getFailCount(empNo);
        
        // 5회 이상이면 DB의 'locked' 컬럼을 1로 업데이트
        if (currentFailCount >= 5) {
            employeeMapper.updateLockedStatus(empNo);
        }
        
        return currentFailCount;
    }
    
    @Transactional
    public void forceLock(String empNo) {
        employeeMapper.updateLockedStatus(empNo);
    }
    
    /**
     * 실패 횟수 초기화 (로그인 성공 시 호출)
     */
    @Transactional
    public void resetFailCount(String empNo) {
        employeeMapper.resetFailCount(empNo);
    }
    
    // 전체 사원 목록 조회
    public List<EmployeeDTO> getAllEmployees() {
        return employeeMapper.findAll();
    }
}