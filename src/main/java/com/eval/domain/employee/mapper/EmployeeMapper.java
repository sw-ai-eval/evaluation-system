package com.eval.domain.employee.mapper;

import com.eval.domain.employee.dto.EmpManageDTO;
import com.eval.domain.employee.dto.EmployeeDTO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;

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
    
    // 관리자용: 비밀번호 초기화 및 계정 잠금 해제
    void resetPasswordAndUnlock(@Param("empNo") String empNo, @Param("encodedPassword") String encodedPassword);
    
    // 전체 사원 목록 조회
    List<EmployeeDTO> findAll();
    List<EmpManageDTO> findAllEmp();
    
    void updateLockedStatus(String empNo);
    
    //사원 페이지 별 조회 용
    List<EmpManageDTO> search(@Param("keyword") String keyword,
            @Param("deptId") String deptId,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("size") int size);
    
    
    int countEmployees(@Param("keyword") String keyword,
            @Param("deptId") String deptId,
            @Param("status") String status);
    
    // 사원 수정 폼 데이터 삽입 용
    EmpManageDTO findByEmpNoDetail(String empNo);
    
    // 마이페이지에서 본인 비밀번호 변경
    void updatePassword(@Param("empNo") String empNo, @Param("newPw") String newPw);
    
}