package com.eval.domain.competency.mapper;
import com.eval.domain.competency.dto.CompetencyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CompetencyMapper {
    
    // 1. 기준년도 리스트 조회
    List<Integer> selectEvalYears();
    
    // 2. 평가 목록 조회
    List<CompetencyDTO.Info> selectEvalList(@Param("year") Integer year, 
                                            @Param("period") String period, 
                                            @Param("currentEmpNo") String currentEmpNo);
    // 3. 대상자 기본 정보 조회
    CompetencyDTO.Info selectCompetencyInfo(@Param("typeId") Integer typeId, 
                                            @Param("empNo") String empNo);
    
    // 4. 역량평가 문항 및 답변 조회
    List<CompetencyDTO.Item> selectCompetencyItems(@Param("typeId") Integer typeId, 
                                                   @Param("empNo") String empNo);

    // 5. 본인 평가 저장
    void updateSelfAnswer(@Param("empNo") String empNo, 
                          @Param("typeId") Integer typeId,
                          @Param("item") CompetencyDTO.Item item);

    // 6. 부서장 1차 평가 - step=1 매핑 존재 확인 및 생성
    int existsFirstMapping(@Param("empNo") String empNo, 
                           @Param("typeId") Integer typeId);

    void insertFirstMapping(@Param("empNo") String empNo, 
                            @Param("typeId") Integer typeId);

    void updateFirstAnswer(@Param("empNo") String empNo, 
                           @Param("typeId") Integer typeId,
                           @Param("item") CompetencyDTO.Item item);

    // 7. 상태 변경
    void updateEvalStatus(@Param("typeId") Integer typeId, 
                          @Param("empNo") String empNo, 
                          @Param("status") int statusCode);
}