
package com.eval.domain.multi.mapper;
 
import java.util.List; import java.util.Map;
 
import org.apache.ibatis.annotations.Mapper;
 
import com.eval.domain.multi.dto.MultiEvalDTO;
import com.eval.domain.multi.dto.MultiEvalDTO.EvalItem;
 
@Mapper 
public interface MultiMapper{
 
	List<MultiEvalDTO> findMultiEval(Map<String, Object> params); 

	MultiEvalDTO findMultiEvalDetail(Map<String, Object> params);

	List<EvalItem> findEvalItems(Map<String, Object> params);
	
}
