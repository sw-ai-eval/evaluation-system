package com.eval.domain.performance.mapper;

import com.eval.domain.performance.dto.PerformanceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface PerformanceMapper {

    List<Integer> selectEvalYears();

    /** 진행 문서 목록 (empNo=null이면 전체 - ADMIN용) */
    List<PerformanceDTO.Info> selectEvalList(
            @Param("year") Integer year,
            @Param("period") String period,
            @Param("empNo") String empNo,
            @Param("deptId") String deptId);

    /** 확정 문서 목록 (final_result_52.status=1인 사원만) */
    List<PerformanceDTO.Info> selectConfirmedList(
            @Param("year") Integer year,
            @Param("period") String period,
            @Param("empNo") String empNo,
            @Param("deptId") String deptId);

    PerformanceDTO.Info selectEvalInfo(@Param("typeId") Integer typeId, @Param("empNo") String empNo);

    List<PerformanceDTO.Item> selectEvalItems(@Param("typeId") Integer typeId, @Param("empNo") String empNo);

    void updateSelfAnswer(@Param("empNo") String empNo, @Param("typeId") Integer typeId, @Param("item") PerformanceDTO.Item item);

    void updateFirstAnswer(@Param("empNo") String empNo, @Param("typeId") Integer typeId, @Param("item") PerformanceDTO.Item item);

    void updateEvalStatus(@Param("typeId") Integer typeId, @Param("empNo") String empNo, @Param("status") Integer status, @Param("step") Integer step);

    /** 평가 기간(start_date, end_date) 조회 */
    Map<String, Object> selectEvalPeriod(@Param("typeId") Integer typeId);
}