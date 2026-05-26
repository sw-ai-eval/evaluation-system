package com.eval.domain.empscore.mapper;

import com.eval.domain.empscore.dto.EmpScoreDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmpScoreMapper {

    List<Integer> selectYears();

    /** 사원 탭 목록 */
    List<EmpScoreDTO.StaffInfo> selectStaffList(
            @Param("year") Integer year,
            @Param("period") String period,
            @Param("empNo") String empNo,
            @Param("deptId") String deptId,
            @Param("currentEmpNo") String currentEmpNo,
            @Param("isAdmin") boolean isAdmin);

    /** 부서장 탭 목록 */
    List<EmpScoreDTO.LeaderInfo> selectLeaderList(
            @Param("year") Integer year,
            @Param("period") String period,
            @Param("empNo") String empNo,
            @Param("deptId") String deptId,
            @Param("currentEmpNo") String currentEmpNo,
            @Param("isAdmin") boolean isAdmin);
}