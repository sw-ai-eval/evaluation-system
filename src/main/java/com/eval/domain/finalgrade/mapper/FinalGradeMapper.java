package com.eval.domain.finalgrade.mapper;

import com.eval.domain.finalgrade.dto.FinalGradeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinalGradeMapper {

    List<Integer> selectYears();

    List<FinalGradeDTO.StaffInfo> selectStaffList(
            @Param("year") Integer year,
            @Param("period") String period,
            @Param("empNo") String empNo,
            @Param("deptId") String deptId);

    List<FinalGradeDTO.LeaderInfo> selectLeaderList(
            @Param("year") Integer year,
            @Param("period") String period,
            @Param("empNo") String empNo,
            @Param("deptId") String deptId);

    List<FinalGradeDTO.DeptStat> selectDeptStat(@Param("year") Integer year);

    /** 부서별 미확정 사원 점수 목록 (등급 재조정용) */
    List<FinalGradeDTO.StaffScore> selectStaffScoreByDept(
            @Param("deptId") String deptId,
            @Param("year") Integer year);

    /** 부서장 전체 점수 목록 (자동 재조정용) */
    List<FinalGradeDTO.StaffScore> selectLeaderScoreList(@Param("year") Integer year);

    void upsertFinalResult(
            @Param("empNo") String empNo,
            @Param("year") Integer year,
            @Param("grade") String grade,
            @Param("finalScore") Integer finalScore,
            @Param("status") int status,
            @Param("reason") String reason,
            @Param("weightRatio") String weightRatio,
            @Param("updatedBy") String updatedBy);

    void confirmFinalResult(
            @Param("empNo") String empNo,
            @Param("year") Integer year,
            @Param("updatedBy") String updatedBy);

    void updateMappingStatus(
            @Param("empNo") String empNo,
            @Param("year") Integer year);

	void updateExecutiveMappingStatus(@Param("empNo") String empNo,@Param("year") Integer year, @Param("updatedBy")  String updatedBy);
}