
package com.eval.domain.multi.mapper;
 
import java.math.BigDecimal;
import java.util.List; import java.util.Map;
 
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eval.domain.evaluator.EvalTargetMapping;
import com.eval.domain.multi.dto.MultiEvalDTO;
import com.eval.domain.multi.dto.MultiEvalDTO.CategoryAvgDto;
import com.eval.domain.multi.dto.MultiEvalDTO.EvalItem;
import com.eval.domain.multi.dto.MultiEvalDTO.MyCategoryScoreDto;
 
@Mapper 
public interface MultiMapper{
 
	List<MultiEvalDTO> findMultiEval(Map<String, Object> params); 

	MultiEvalDTO findMultiEvalDetail(Map<String, Object> params);

	List<EvalItem> findEvalItems(Map<String, Object> params);
	
	EvalTargetMapping findTargetMapping(Map<String, Object> params);

	List<MultiEvalDTO> findMultiProgressEval(Map<String, Object> params);

	List<MultiEvalDTO> findMultiCompletedEval(Map<String, Object> params);
	
	List<MultiEvalDTO> findAllDeptMultiProgressEval(Map<String, Object> params);

	List<Integer> findAvailableYears();

    List<MultiEvalDTO.EvalItem> selectEvalItems(
            @Param("evaluateeNo") String evaluateeNo,
            @Param("evalTypeId") Integer evalTypeId
    );
    
    List<MyCategoryScoreDto>findCategoryScores(String evaluateeNo, Long evalTypeId);
    
    List<CategoryAvgDto> findMyAvgCategoryScores(String evaluateeNo, Long evalTypeId);
    
    List<CategoryAvgDto> findAllAvgCategoryScores(Long evalTypeId);

	List<MultiEvalDTO> findAllDeptMultiCompletedEval(Map<String, Object> params);

	int countAllDeptMultiProgressEval(Map<String, Object> params);

	int countMultiProgressEval(Map<String, Object> params);

	int countAllDeptMultiCompleteEval(Map<String, Object> params);

	int countMultiCompleteEval(Map<String, Object> params);

	
}
