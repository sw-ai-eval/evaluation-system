package com.eval.domain.competency.mapper;

import com.eval.domain.competency.dto.CompetencyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CompetencyMapper {
    
    List<Integer> selectEvalYears();
    
    List<CompetencyDTO.Info> selectEvalList(@Param("year") Integer year, 
                                           @Param("period") String period, 
                                           @Param("currentEmpNo") String currentEmpNo);

    CompetencyDTO.Info selectCompetencyInfo(@Param("typeId") Integer typeId, @Param("empNo") String empNo);
    
    List<CompetencyDTO.Item> selectCompetencyItems(@Param("typeId") Integer typeId, @Param("empNo") String empNo);

    void updateSelfAnswer(@Param("empNo") String empNo, @Param("typeId") Integer typeId, @Param("item") CompetencyDTO.Item item);
    void updateFirstAnswer(@Param("empNo") String empNo, @Param("typeId") Integer typeId, @Param("item") CompetencyDTO.Item item);

    void updateEvalStatus(@Param("typeId") Integer typeId, @Param("empNo") String empNo, @Param("status") int status, @Param("step") int step);
}