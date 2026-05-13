
package com.eval.domain.multi.service;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List; import java.util.Map;
 
import org.springframework.stereotype.Service;

import com.eval.domain.evaluator.service.EvaluatorService;
import com.eval.domain.multi.MultiEvalAnswer;
import com.eval.domain.multi.MultiEvalAnswerRepository;
import com.eval.domain.multi.dto.MultiEvalDTO; 
import com.eval.domain.multi.mapper.MultiMapper;
 
import lombok.RequiredArgsConstructor;
 
 @Service 
 @RequiredArgsConstructor 
 public class MultiService {

     private final MultiMapper multiMapper;
     private final MultiEvalAnswerRepository multiEvalAnswerRepository;
     private final EvaluatorService evaluatorService;

     public List<MultiEvalDTO> getMultiList(String empNo, String position) {
    	    Map<String, Object> params = new HashMap<>();
    	    params.put("userNo", empNo);
    	    params.put("position", position); // Mapper 조건용
    	    return multiMapper.findMultiEval(params);
    	}
     
     public MultiEvalDTO getMultiDetail(Long evalTypeId, String empNo, String loginEmpNo, String position) {
    	    Map<String, Object> params = new HashMap<>();
    	    params.put("evalTypeId", evalTypeId);
    	    params.put("empNo", empNo);
    	    params.put("loginEmpNo", loginEmpNo);
    	    params.put("position", position);

    	    MultiEvalDTO dto = multiMapper.findMultiEvalDetail(params);
    	    System.out.println("dto = " + dto);
    	    
    	    System.out.println("evalTypeId = " + params.get("evalTypeId"));
    	    System.out.println("empNo = " + params.get("empNo"));
    	    
    	    List<MultiEvalDTO.EvalItem> items = multiMapper.findEvalItems(params);
    	    System.out.println("dto = " + dto);
    	    dto.setItems(items);
    	    
    	    // 카테고리별 그룹화
    	    Map<String, MultiEvalDTO.CategorySummary> categoryMap = new LinkedHashMap<>();
    	    for (MultiEvalDTO.EvalItem item : items) {
    	        categoryMap.compute(item.getCategoryName(), (k, summary) -> {
    	            if (summary == null) {
    	                summary = new MultiEvalDTO.CategorySummary();
    	                summary.setCategoryName(k);
    	                summary.setTotalWeight(item.getWeight());
    	                summary.setCombinedGuide(item.getExplanation() != null ? item.getExplanation() : "");
    	                summary.setItems(new ArrayList<>());
    	            } else {
    	                summary.setTotalWeight(summary.getTotalWeight() + item.getWeight());
    	                summary.setCombinedGuide(summary.getCombinedGuide() + "\n" + item.getExplanation());
    	            }
    	            System.out.println("summary.getTotalWeight():"+summary.getTotalWeight());
    	            summary.getItems().add(item);
    	            return summary;
    	        });
    	    }

    	    dto.setCategorySummaries(new ArrayList<>(categoryMap.values()));
    	    System.out.println("===== categorySummaries 출력 =====");
    	    dto.getCategorySummaries().forEach(cs -> {
    	        System.out.println("카테고리명: " + cs.getCategoryName());
    	        System.out.println("총 배점: " + cs.getTotalWeight());
    	        System.out.println("가이드라인:\n" + cs.getCombinedGuide());
    	        System.out.println("항목 수: " + (cs.getItems() != null ? cs.getItems().size() : 0));
    	        System.out.println("-----------------------------");
    	    });
    	    return dto;

    	}
     
     
     	public void saveAnswers(List<MultiEvalAnswer> answers, String evaluatorNo, String evaluateeNo, Integer evalTypeId) {
    	    multiEvalAnswerRepository.saveAll(answers); 
    	    evaluatorService.updateStatusToTwo(evaluatorNo, evaluateeNo, evalTypeId);
    	}
     
     
     
     
     
     
 }