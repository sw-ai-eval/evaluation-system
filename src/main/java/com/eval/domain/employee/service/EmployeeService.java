package com.eval.domain.employee.service;

import com.eval.domain.employee.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public int increaseFailCount(String empNo) {
        // 1. 사원의 현재 실패 횟수를 1 증가시키는 쿼리 실행
        employeeMapper.incrementFailCount(empNo);
        
        // 2. 증가된 현재 실패 횟수를 다시 조회하여 반환
        return employeeMapper.getFailCount(empNo);
    }
    
    @Transactional
    public void resetFailCount(String empNo) {
        // 로그인 성공 시 실패 횟수를 0으로 초기화할 때 사용
        employeeMapper.resetFailCount(empNo);
    }
}