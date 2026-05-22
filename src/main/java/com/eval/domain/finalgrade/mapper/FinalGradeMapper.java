package com.eval.domain.finalgrade.mapper;

import com.eval.domain.finalgrade.dto.FinalGradeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinalGradeMapper {

    List<Integer> selectYears();

    /** 사원 탭 목록 (position = '부서원') */
    List<FinalGradeDTO.StaffInfo> selectStaffList(
            @Param("year") Integer year,
            @Param("period") String period,
            @Param("empNo") String empNo);

    /** 부서장 탭 목록 (position = '부서장') */
    List<FinalGradeDTO.LeaderInfo> selectLeaderList(
            @Param("year") Integer year,
            @Param("period") String period,
            @Param("empNo") String empNo);

    /** 부서별 등급 통계 */
    List<FinalGradeDTO.DeptStat> selectDeptStat(@Param("year") Integer year);

    /** 단건 등급 UPSERT */
    void upsertFinalResult(
            @Param("empNo") String empNo,
            @Param("year") Integer year,
            @Param("grade") String grade,
            @Param("finalScore") Integer finalScore,
            @Param("status") int status,
            @Param("reason") String reason,
            @Param("weightRatio") String weightRatio,
            @Param("updatedBy") String updatedBy);

    /** 최종 확정 (status=1) */
    void confirmFinalResult(
            @Param("empNo") String empNo,
            @Param("year") Integer year,
            @Param("updatedBy") String updatedBy);

    /** eval_target_mapping step=0 status=2 업데이트 */
    void updateMappingStatus(
            @Param("empNo") String empNo,
            @Param("year") Integer year);
}