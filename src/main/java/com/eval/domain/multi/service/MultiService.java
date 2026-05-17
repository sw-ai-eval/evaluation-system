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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.evaluator.EvalTargetMapping;
import com.eval.domain.evaluator.repository.EvaluatorRepository;
import com.eval.domain.evaluator.service.EvaluatorService;
import com.eval.domain.multi.EvalSummaryChart;
import com.eval.domain.multi.EvalSummaryRepository;
import com.eval.domain.multi.MultiEvalAnswer;
import com.eval.domain.multi.MultiEvalAnswerRepository;
import com.eval.domain.multi.dto.MultiEvalDTO;
import com.eval.domain.multi.dto.MultiEvalDTO.MultiChartDto;
import com.eval.domain.multi.mapper.MultiMapper;
import com.eval.domain.performance.dto.PerformanceDTO.EvalType;

import lombok.RequiredArgsConstructor;
 
 @Service 
 @RequiredArgsConstructor 
 public class MultiService {

     private final MultiMapper multiMapper;
     private final MultiEvalAnswerRepository multiEvalAnswerRepository;
     private final EvaluatorService evaluatorService;
     private final EvalSummaryRepository evalSummaryRepository;
     private final EvaluatorRepository evaluatorRepository;
     private final EmployeeRepository employeeRepository;
     
     //진행전, 진행중 평가 리스트
     public List<MultiEvalDTO> getMultiProgressList(String empNo, String position) {
    	    Map<String, Object> params = new HashMap<>();
    	    params.put("userNo", empNo);
    	    params.put("position", position); // Mapper 조건용
    	    return multiMapper.findMultiProgressEval(params);
    	}
     
     //완료 평가 리스트
     public List<MultiEvalDTO> getMultiCompletedList(String empNo, String position) {
 	    Map<String, Object> params = new HashMap<>();
 	    params.put("userNo", empNo);
 	    params.put("position", position); // Mapper 조건용
 	    return multiMapper.findMultiCompletedEval(params);
 	}
     
     // 세부 패널 화면
     public MultiEvalDTO getMultiDetail(Long evalTypeId, String evaluateeNo, String evaluatorNo, String position) {
    	    Map<String, Object> params = new HashMap<>();
    	    params.put("evalTypeId", evalTypeId);
    	    params.put("evaluateeNo", evaluateeNo);
    	    params.put("evaluatorNo", evaluatorNo);
    	    params.put("position", position);

    	    // status를 문자열로 변환해서 Mapper로 전달

    	    EvalTargetMapping etm = multiMapper.findTargetMapping(params);// 필요 시 별도 Mapper 호출
    	    
    	    String statusStr = etm != null ? String.valueOf(etm.getStatus()) : "0"; // null이면 기본 '0'
    	    params.put("statusStr", statusStr);

    	    MultiEvalDTO dto = multiMapper.findMultiEvalDetail(params);
    	    
    	    System.out.println("dto = " + dto);
    	    
    	    System.out.println("===== findMultiEvalDetail 결과 =====");
    	    System.out.println("evalTypeId: " + dto.getEvalTypeId());
    	    System.out.println("evalTypeName: " + dto.getEvalTypeName());
    	    System.out.println("evaluatorNo: " + dto.getEvaluatorNo());
    	    System.out.println("evaluatorName: " + dto.getEvaluatorName());
    	    System.out.println("evaluateeNo: " + dto.getEvaluateeNo());
    	    System.out.println("deptId: " + dto.getDeptId());
    	    System.out.println("deptName: " + dto.getDeptName());
    	    System.out.println("startDate: " + dto.getStartDate());
    	    System.out.println("endDate: " + dto.getEndDate());
    	    System.out.println("statusName: " + dto.getStatusName());
    	    System.out.println("totalScore: " + dto.getTotalScore());
    	    // items는 아직 안 세팅되었으므로 null일 수 있음
    	    System.out.println("items: " + dto.getItems());
    	    System.out.println("===== 끝 =====");
    	    
    	    List<MultiEvalDTO.EvalItem> items = multiMapper.findEvalItems(params);
    	    System.out.println("dto = " + dto);
    	    dto.setItems(items);
    	    System.out.println("===== items 데이터 확인 =====");
    	    for (MultiEvalDTO.EvalItem item : items) {
    	        System.out.println("QuestionId: " + item.getQuestionId() +
    	                           ", Category: " + item.getCategoryName() +
    	                           ", Score: " + item.getScore() +
    	                           ", Content: " + item.getContent() +
    	                           ", Weight: " + item.getWeight() +
    	                           ", MappingId: " + item.getMappingId() +
    	                           ", Explanation: " + item.getExplanation());
    	    }
    	    System.out.println("===== items 끝 =====");
    	    
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

    	    // 카테고리별 그룹화
    	    Map<String, MultiEvalDTO.CategorySummary> categoryMap = new LinkedHashMap<>();
    	    for (MultiEvalDTO.EvalItem item : items) {
    	        categoryMap.compute(item.getCategoryName(), (k, summary) -> {
    	            if (summary == null) {
    	                summary = new MultiEvalDTO.CategorySummary();
    	                summary.setCategoryName(k);
    	                summary.setTotalWeight(item.getWeight() != null ? item.getWeight() : 0);
    	                summary.setCombinedGuide(item.getExplanation() != null ? item.getExplanation() : "");
    	                summary.setItems(new ArrayList<>());
    	            } else {
    	                summary.setTotalWeight(summary.getTotalWeight() + (item.getWeight() != null ? item.getWeight() : 0));
    	                summary.setCombinedGuide(summary.getCombinedGuide() + "\n" + (item.getExplanation() != null ? item.getExplanation() : ""));
    	            }
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
    	    
    	    System.out.println("DTO statusName: " + dto.getStatusName());
    	    
    	    int totalScore = items.stream()
    	            .map(MultiEvalDTO.EvalItem::getScore)
    	            .filter(Objects::nonNull)
    	            .mapToInt(Integer::intValue)
    	            .sum();

    	    dto.setTotalScore(totalScore);
    	    
    	    dto.setChart(getLeaderChart(evalTypeId, evaluateeNo));
    	    
    	    System.out.println("totalScore: "+totalScore);
    	    
    	    return dto;

    	}
     
     	
     // 평가 입력 내용 저장
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
		
		evaluatorService.updateStatusToTwo( evaluatorNo,evaluateeNo,evalTypeId);
	}
     	
     //임시 저장
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
     
	 // 차트에 사용할 점수 합산, 평균   
     @Transactional
     public EvalSummaryChart saveSummary(String evaluateeNo, Integer evalTypeId) {

         // 1. 해당 부서장의 총점
         BigDecimal myTotal =multiEvalAnswerRepository.getTotalScore( evaluateeNo,  evalTypeId);

         // 2. 해당 부서장의 평균 점수
         BigDecimal myAvg = multiEvalAnswerRepository.getMyAvgScore( evaluateeNo, evalTypeId );

         // 3. 기존 summary 조회
         EvalSummaryChart summary =evalSummaryRepository.findByEvaluateeNoAndEvalTypeId( evaluateeNo, evalTypeId).orElseGet(EvalSummaryChart::new);

         // 4. 값 세팅
         summary.setEvaluateeNo(evaluateeNo);
         summary.setEvalTypeId(evalTypeId);

         summary.setTotalScore(myTotal != null ? myTotal : BigDecimal.ZERO);

         summary.setAvgScore(myAvg != null ? myAvg : BigDecimal.ZERO);

         summary.setMaxGivenScore(BigDecimal.ZERO);

         summary.setEvaluatedAt(LocalDateTime.now());

         // 5. 저장
         return evalSummaryRepository.save(summary);
     }
     
     public MultiChartDto getLeaderChart( Long evalTypeId, String leaderNo) {
    	    // 내 총점
    	    BigDecimal myTotalScore =evalSummaryRepository.findMyTotalScore( evalTypeId,leaderNo);

    	    // 내 평균 점수
    	    BigDecimal myAvgScore =evalSummaryRepository.findMyAvgScore( evalTypeId, leaderNo);

    	    // 전체 부서장 평균들의 평균
    	    BigDecimal allLeaderAvgScore = evalSummaryRepository.findAllLeaderAvgScore( evalTypeId );

    	    // DTO 조립
    	    MultiEvalDTO.MultiChartDto dto =new MultiEvalDTO.MultiChartDto();

    	    dto.setEvaluateeNo(leaderNo);

    	    dto.setMyTotalScore( myTotalScore != null ? myTotalScore : BigDecimal.ZERO);

    	    dto.setMyAvgScore(myAvgScore != null ? myAvgScore: BigDecimal.ZERO);

    	    dto.setAllLeaderAvgScore( allLeaderAvgScore != null? allLeaderAvgScore : BigDecimal.ZERO );

    	    return dto;
    	}
     
 }