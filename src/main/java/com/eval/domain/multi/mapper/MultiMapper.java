
package com.eval.domain.multi.mapper;
 
import java.math.BigDecimal;
import java.util.List; import java.util.Map;
 
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eval.domain.evaluator.EvalTargetMapping;
import com.eval.domain.multi.dto.MultiEvalDTO;
import com.eval.domain.multi.dto.MultiEvalDTO.EvalItem;
 
@Mapper 
public interface MultiMapper{
 
	List<MultiEvalDTO> findMultiEval(Map<String, Object> params); 

	MultiEvalDTO findMultiEvalDetail(Map<String, Object> params);

	List<EvalItem> findEvalItems(Map<String, Object> params);
	
	EvalTargetMapping findTargetMapping(Map<String, Object> params);

	List<MultiEvalDTO> findMultiProgressEval(Map<String, Object> params);

	List<MultiEvalDTO> findMultiCompletedEval(Map<String, Object> params);
	
    BigDecimal findMyTotalScore(@Param("evaluateeNo") String evaluateeNo, @Param("evalTypeId") Integer evalTypeId);

    BigDecimal findAvgTotalScore(@Param("evalTypeId") Integer evalTypeId);

}
