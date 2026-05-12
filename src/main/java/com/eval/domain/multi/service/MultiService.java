
package com.eval.domain.multi.service;
 
import java.util.HashMap; import java.util.List; import java.util.Map;
 
import org.springframework.stereotype.Service;
 
import com.eval.domain.multi.dto.MultiEvalDTO; 
import com.eval.domain.multi.mapper.MultiMapper;
 
import lombok.RequiredArgsConstructor;
 
 @Service 
 @RequiredArgsConstructor 
 public class MultiService {

     private final MultiMapper multiMapper;

     public List<MultiEvalDTO> getMultiList(String empNo, String position) {
    	    Map<String, Object> params = new HashMap<>();
    	    params.put("userNo", empNo);
    	    params.put("position", position); // Mapper 조건용
    	    return multiMapper.findMultiEval(params);
    	}
     
     public MultiEvalDTO getMultiDetail(Long typeId, String empNo, String loginEmpNo, String position) {
    	    Map<String, Object> params = new HashMap<>();
    	    params.put("typeId", typeId);
    	    params.put("empNo", empNo);
    	    params.put("loginEmpNo", loginEmpNo);
    	    params.put("position", position);

    	    MultiEvalDTO dto = multiMapper.findMultiEvalDetail(params);
    	    System.out.println("dto = " + dto);
    	    
    	    System.out.println("typeId = " + params.get("typeId"));
    	    System.out.println("empNo = " + params.get("empNo"));
    	    
    	    List<MultiEvalDTO.EvalItem> items = multiMapper.findEvalItems(params);
    	    System.out.println("dto = " + dto);
    	    dto.setItems(items);

    	    return dto;
    	}
 }