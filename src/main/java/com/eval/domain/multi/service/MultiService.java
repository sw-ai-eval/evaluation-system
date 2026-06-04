package com.eval.domain.multi.service;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List; import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.evaluator.EvalTargetMapping;
import com.eval.domain.evaluator.repository.EvaluatorRepository;
import com.eval.domain.evaluator.service.EvaluatorService;
import com.eval.domain.multi.EvalCategorySummary;
import com.eval.domain.multi.EvalSummaryChart;
import com.eval.domain.multi.MultiEvalAnswer;
import com.eval.domain.multi.dto.MultiEvalDTO;
import com.eval.domain.multi.dto.PageResponse;
import com.eval.domain.multi.dto.MultiEvalDTO.CategoryAvgDto;
import com.eval.domain.multi.dto.MultiEvalDTO.MultiChartDto;
import com.eval.domain.multi.dto.MultiEvalDTO.MyCategoryScoreDto;
import com.eval.domain.multi.mapper.MultiMapper;
import com.eval.domain.multi.repository.EvalCategorySummaryRepository;
import com.eval.domain.multi.repository.EvalSummaryRepository;
import com.eval.domain.multi.repository.MultiEvalAnswerRepository;
import com.eval.domain.performance.dto.PerformanceDTO.EvalType;

import lombok.RequiredArgsConstructor;
 
 @Service 
 @RequiredArgsConstructor 
 public class MultiService {

     private final MultiMapper multiMapper;
     private final MultiEvalAnswerRepository multiEvalAnswerRepository;
     private final EvaluatorService evaluatorService;
     private final EvalCategorySummaryRepository evalCategorySummaryRepository;
     private final EvaluatorRepository evaluatorRepository;
     
     public List<Integer> getAvailableYears() {
    	    return multiMapper.findAvailableYears();
    }
     
     //진행 문서
     public PageResponse<MultiEvalDTO> getMultiProgressList(
    		 	String empNo, 
  	        	String position,
    	        Integer year,
    	        String period,
    	        Pageable pageable,
    	        String role
    	) {
    	    Map<String, Object> params = new HashMap<>();
    	    params.put("year", year);
    	    params.put("period", period);
    	    params.put("offset", pageable.getPageNumber() * pageable.getPageSize());
    	    params.put("size", pageable.getPageSize());
    	    

    	    System.out.println("========== [MULTI PROGRESS PARAMS] ==========");
    	    System.out.println("role = " + role);
    	    System.out.println("empNo = " + empNo);
    	    System.out.println("position = " + position);
    	    System.out.println("params = " + params);
    	    
    	    List<MultiEvalDTO> list=null;
    	    int total =0;
    	    if("ADMIN".equals(role)) {
    	    	System.out.println("▶ ADMIN branch");
    	    	
    	    	list = multiMapper.findAllDeptMultiProgressEval(params);
    	    	total = multiMapper.countAllDeptMultiProgressEval(params);
    	    }
    	    else {
    	    	params.put("userNo", empNo);
    	 	    params.put("position", position);
    	 	    
    	 	   System.out.println("▶ USER branch");
    	        System.out.println("params(with user) = " + params);
    	        
    	    	list = multiMapper.findMultiProgressEval(params);
    	    	total = multiMapper.countMultiProgressEval(params);
    	    }

    	    System.out.println("result list size = " + (list != null ? list.size() : 0));
    	    System.out.println("total = " + total);

    	    return new PageResponse<>(
    	            list,
    	            total,
    	            pageable.getPageNumber(),
    	            pageable.getPageSize()
    	    );
    	}
     
     // 확정 문서
     public PageResponse<MultiEvalDTO> getMultiCompletedList(
    		String empNo, 
  	        String position,
    		Integer year,
 	        String period,
 	        Pageable pageable,
 	        String role
 	) {
 	    Map<String, Object> params = new HashMap<>();
 	    params.put("year", year);
 	    params.put("period", period);
 	    params.put("offset", pageable.getOffset());
 	    params.put("size", pageable.getPageSize());
 	    
 	    
 	   params.put("year", year);
	    params.put("period", period);
	    params.put("offset", pageable.getPageNumber() * pageable.getPageSize());
	    params.put("size", pageable.getPageSize());
 	    
 	    List<MultiEvalDTO> list=null;
 	    int total =0;
 	    if(role.equals("ADMIN")) {
 	    	list = multiMapper.findAllDeptMultiCompletedEval(params);
 	    	total = multiMapper.countAllDeptMultiCompleteEval(params);
 	    	
 	    	System.out.println("▶ ADMIN branch");
 	    	
 	    }
 	    else {
 	    	params.put("userNo", empNo);
 	 	    params.put("position", position);
 	    	list = multiMapper.findMultiCompletedEval(params);
 	    	total = multiMapper.countMultiCompleteEval(params);
 	    	
 	    	System.out.println("▶ USER branch");
	        System.out.println("params(with user) = " + params);
 	    }


 	   System.out.println("result list size = " + (list != null ? list.size() : 0));
	    System.out.println("total = " + total);


 	    return new PageResponse<>(
 	            list,
 	            total,
 	            pageable.getPageNumber(),
 	            pageable.getPageSize()
 	    );
 	}
     
     // 세부 패널 화면
     public MultiEvalDTO getMultiDetail(Long evalTypeId, String evaluatorNo,String evaluateeNo,  String position) {
    	    Map<String, Object> params = new HashMap<>();
    	    params.put("evalTypeId", evalTypeId);
    	    params.put("evaluateeNo", evaluateeNo);
    	    params.put("evaluatorNo", evaluatorNo);
    	    params.put("position", position);

    	    // status를 문자열로 변환해서 Mapper로 전달

    	    EvalTargetMapping etm = multiMapper.findTargetMapping(params);// 필요 시 별도 Mapper 호출
    	    
    	    System.out.println("mappingId : " + etm.getId()); // 매핑 아이디 잘 나옴
    	    
    	    String statusStr = etm != null ? String.valueOf(etm.getStatus()) : "0"; // null이면 기본 '0'
    	    params.put("statusStr", statusStr);


    	    MultiEvalDTO dto = multiMapper.findMultiEvalDetail(params);
    	    
    	    
    	    List<MultiEvalDTO.EvalItem> items = multiMapper.findEvalItems(params);
    	    dto.setItems(items);
    	    
    	    
    	    if(!"평가전".equals(dto.getStatusName())) {
    	    // 각 문항에 기존 답변(점수/피드백) 세팅
	    	    for (MultiEvalDTO.EvalItem item : items) {
	    	    	Long mappingId = etm != null ? etm.getId() : null;
	    	        item.setMappingId(mappingId);
	
	    	        // 기존 답변 조회
	    	        Optional<MultiEvalAnswer> answerOpt = multiEvalAnswerRepository.findByQuestionIdAndMappingId(item.getQuestionId(), mappingId);
	    	        if (answerOpt.isPresent()) {
	    	            MultiEvalAnswer answer = answerOpt.get();
	    	            if (answer.getScore() != null) {
	    	                item.setScore(answer.getScore().intValue());
	    	            }
	    	            if (answer.getContent() != null) {
	    	                item.setContent(answer.getContent()); // 점수형 피드백 또는 서술형 의견
	    	            }
	    	            System.out.println("QuestionId: " + item.getQuestionId() +
	                            ", Score: " + item.getScore() +
	                            ", Content: " + item.getContent());
	
	    	        }
	    	    }
    	    }

    	    // 카테고리별 그룹화
    	 // statusName 확인 후 "평가 완료"이면 카테고리 그룹화 건너뜀
    	    List<Integer> totalWeightList = new ArrayList<>();

    	    if (!"평가완료".equals(dto.getStatusName())) {
    	        // 평가 미완료: 기존처럼 CategorySummary 생성
    	        Map<String, MultiEvalDTO.CategorySummary> categoryMap = new LinkedHashMap<>();
    	        for (MultiEvalDTO.EvalItem item : items) {
    	            MultiEvalDTO.CategorySummary summary = categoryMap.get(item.getCategoryName());
    	            if (summary == null) {
    	                summary = new MultiEvalDTO.CategorySummary();
    	                summary.setCategoryName(item.getCategoryName());
    	                summary.setTotalWeight(item.getWeight() != null ? item.getWeight() : 0);
    	                summary.setCombinedGuide(item.getExplanation() != null ? item.getExplanation() : "");
    	                summary.setItems(new ArrayList<>());
    	                categoryMap.put(item.getCategoryName(), summary);
    	            } else {
    	                summary.setTotalWeight(summary.getTotalWeight() + (item.getWeight() != null ? item.getWeight() : 0));
    	                summary.setCombinedGuide(summary.getCombinedGuide() + "\n" + (item.getExplanation() != null ? item.getExplanation() : ""));
    	            }
    	            summary.getItems().add(item);
    	        }
    	        dto.setCategorySummaries(new ArrayList<>(categoryMap.values()));

    	        // totalWeight 리스트 생성
    	        for (MultiEvalDTO.CategorySummary summary : categoryMap.values()) {
    	            totalWeightList.add(summary.getTotalWeight());
    	        }
    	    } else {
    	        // 평가 완료: totalWeight만 계산
    	        Map<String, Integer> totalWeightMap = new LinkedHashMap<>();
    	        for (MultiEvalDTO.EvalItem item : items) {
    	            int weight = item.getWeight() != null ? item.getWeight() : 0;
    	            totalWeightMap.put(item.getCategoryName(), totalWeightMap.getOrDefault(item.getCategoryName(), 0) + weight);
    	        }

    	        List<MultiEvalDTO.CategorySummary> summaries = new ArrayList<>();
    	        for (Map.Entry<String, Integer> entry : totalWeightMap.entrySet()) {
    	            MultiEvalDTO.CategorySummary summary = new MultiEvalDTO.CategorySummary();
    	            summary.setCategoryName(entry.getKey());
    	            summary.setTotalWeight(entry.getValue());
    	            summaries.add(summary);

    	            // totalWeight 리스트에 추가
    	            totalWeightList.add(entry.getValue());
    	        }
    	        dto.setCategorySummaries(summaries);
    	    }
 
    	    
    	    
    	    int totalScore = items.stream()
    	            .map(MultiEvalDTO.EvalItem::getScore)
    	            .filter(Objects::nonNull)
    	            .mapToInt(Integer::intValue)
    	            .sum();

    	    dto.setTotalScore(totalScore);
    	    
    	    for (Integer weight : totalWeightList) {
    	        System.out.println(weight);
    	    }
    	    
    	    dto.setChart(makeChart(evaluateeNo, evalTypeId, totalWeightList));
    	    
    	    
    	    return dto;

    	}
     
     public boolean ifFinishSelfEval(String empNo) { 	
    	 
    	 boolean complete= evaluatorRepository.existsByEvaluatorNoAndStepAndStatus(empNo,0,0);
    	 System.out.println("boolean: "+complete);
    	 return complete;
     }
    
     // 평가 입력 내용 저장
     @Transactional
     public void saveAnswers(List<MultiEvalAnswer> answers,String evaluatorNo,String evaluateeNo, Integer evalTypeId) {

		answers.forEach(ans -> {

		    System.out.println("mappingId = " + ans.getMappingId());
		    System.out.println("questionId = " + ans.getQuestionId());
		    
		Optional<MultiEvalAnswer> exist = multiEvalAnswerRepository.findByMappingIdAndQuestionId( ans.getMappingId(), ans.getQuestionId() );
		
	
		if (exist.isPresent()) {
				
				 MultiEvalAnswer updateAns = exist.get();
				
				 updateAns.setScore(ans.getScore());
				 updateAns.setContent(ans.getContent());
				
				 multiEvalAnswerRepository.save(updateAns);
			
			} else {
				
				 ans.setCreatedAt(LocalDateTime.now());
				
				 multiEvalAnswerRepository.save(ans);
			}
		});
		
		saveChartSummary(evaluatorNo, evaluateeNo, evalTypeId);
		
		evaluatorService.updateStatusToTwo( evaluatorNo,evaluateeNo,evalTypeId);
	}
     	
     //임시 저장
     @Transactional
     public void temporarySaveAnswers(List<MultiEvalAnswer> answers,String evaluatorNo,String evaluateeNo,Integer evalTypeId) {
		
		answers.forEach(ans -> { 
			Optional<MultiEvalAnswer> exist = multiEvalAnswerRepository.findByMappingIdAndQuestionId( ans.getMappingId(), ans.getQuestionId() );
		
			if (exist.isPresent()) {
				
				MultiEvalAnswer updateAns = exist.get();
				
				updateAns.setScore(ans.getScore());
				updateAns.setContent(ans.getContent());
				
				multiEvalAnswerRepository.save(updateAns);
				
			} else {
			
				ans.setCreatedAt(LocalDateTime.now());
				
				multiEvalAnswerRepository.save(ans);
			}
		});
		
		evaluatorService.updateStatusToOne( evaluatorNo, evaluateeNo, evalTypeId
		);
	}
     
 

     @Transactional
     public List<EvalCategorySummary> saveChartSummary(
             String evaluatorNo,
             String evaluateeNo,
             Integer evalTypeId) {

         Long evalTypeIdLong = evalTypeId.longValue();

         // 항상 전체를 다시 계산해서 가져옴
         List<MyCategoryScoreDto> categoryScores =
                 multiMapper.findCategoryScores(evaluateeNo, evalTypeIdLong);

         List<EvalCategorySummary> summaries = new ArrayList<>();

         for (MyCategoryScoreDto dto : categoryScores) {

             String categoryName = dto.getCategoryName();

             BigDecimal totalScore = BigDecimal.valueOf(dto.getTotalScore());
             int evaluatorCount = dto.getEvaluatorCount();

             // 기존 row 조회 (있으면 update, 없으면 insert)
             EvalCategorySummary summary =
                     evalCategorySummaryRepository
                             .findByEvaluateeNoAndEvalTypeIdAndCategoryName(
                                     evaluateeNo, evalTypeIdLong, categoryName)
                             .orElse(null);

             if (summary == null) {
                 summary = new EvalCategorySummary();
                 summary.setEvaluateeNo(evaluateeNo);
                 summary.setEvalTypeId(evalTypeIdLong);
                 summary.setCategoryName(categoryName);
             }

             summary.setScore(totalScore);

             BigDecimal avgScore = evaluatorCount == 0
                     ? BigDecimal.ZERO
                     : totalScore.divide(
                         BigDecimal.valueOf(evaluatorCount),
                         2,
                         RoundingMode.HALF_UP
                     );

             summary.setAvgScore(avgScore);
             summary.setEvaluatedAt(LocalDateTime.now());

             evalCategorySummaryRepository.save(summary);
             summaries.add(summary);
         }

         return summaries;
     }
     
     public MultiChartDto makeChart(String evaluateeNo, Long evalTypeId, List<Integer> totalWeightList) {

    	    MultiChartDto chart = new MultiChartDto();

    	    List<CategoryAvgDto> myScores = multiMapper.findMyAvgCategoryScores(evaluateeNo, evalTypeId);

    	    List<CategoryAvgDto> allScores = multiMapper.findAllAvgCategoryScores(evalTypeId);

    	    for (CategoryAvgDto myDto : myScores) {
    	        String category = myDto.getCategoryName();
    	        chart.getLabels().add(category);
    	        chart.getMyAvgScores().add(myDto.getAvgScore());
 
    	        double allAvg = allScores.stream()
    	                .filter(a -> a.getCategoryName().equals(category))
    	                .map(CategoryAvgDto::getAvgScore)
    	                .findFirst()
    	                .orElse(0.0); // 없으면 0
    	        chart.getAllAvgScores().add(allAvg);
    	    }
    	    if (totalWeightList != null) {
    	        chart.getMaxScores().addAll(totalWeightList);
    	    }
    	    for (Integer weight : totalWeightList) {
    	        System.out.println(weight);
    	    }

    	    return chart;
    	}

 }