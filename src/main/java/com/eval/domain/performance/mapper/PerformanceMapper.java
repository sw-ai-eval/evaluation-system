package com.eval.domain.performance.mapper;

import com.eval.domain.performance.dto.PerformanceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PerformanceMapper {
    
    // 1. 기준년도 리스트 조회 (중복 제거된 year 필드)
    List<Integer> selectEvalYears();

    // 2. 좌측 사원 목록 조회 (검색 조건: 년도와 기간 문자열)
    List<PerformanceDTO.Info> selectEvalList(@Param("year") Integer year, @Param("period") String period);
    
    // 3. 우측 사원 상세 기본 정보 조회
    PerformanceDTO.Info selectEvalInfo(@Param("typeId") Integer typeId, @Param("empNo") String empNo);
    
    // 4. 우측 사원 상세 문항 및 기존 답변 조회
    List<PerformanceDTO.Item> selectEvalItems(@Param("typeId") Integer typeId, @Param("empNo") String empNo);
    
    // 5. 본인 평가(SELF) 데이터 업데이트
    void updateSelfAnswer(@Param("empNo") String empNo, @Param("item") PerformanceDTO.Item item);
    
    // 6. 1차 평가(FIRST, 부서장) 데이터 업데이트
    void updateFirstAnswer(@Param("empNo") String empNo, @Param("item") PerformanceDTO.Item item);
    
    // 7. 문서 진행 상태(status) 업데이트
    void updateEvalStatus(@Param("typeId") Integer typeId, @Param("empNo") String empNo, @Param("status") Integer status);
}