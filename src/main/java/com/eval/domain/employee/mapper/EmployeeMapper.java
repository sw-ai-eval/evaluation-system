package com.eval.domain.employee.mapper;

import com.eval.domain.employee.dto.EmployeeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmployeeMapper {
    
    // 사번으로 사원 정보 조회
    EmployeeDTO findByEmpNo(@Param("empNo") String empNo);
    
    // 로그인 실패 횟수 1 증가
    void incrementFailCount(@Param("empNo") String empNo);
    
    // 현재 실패 횟수 조회
    int getFailCount(@Param("empNo") String empNo);
    
    // 로그인 성공 시 실패 횟수 초기화
    void resetFailCount(@Param("empNo") String empNo);
}