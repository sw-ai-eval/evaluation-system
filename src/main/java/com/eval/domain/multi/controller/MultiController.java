
package com.eval.domain.multi.controller;
  
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller; 
 import org.springframework.ui.Model; 
 import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.eval.domain.multi.MultiEvalAnswer;
import com.eval.domain.multi.dto.MultiEvalDTO; 
  import com.eval.domain.multi.service.MultiService;
  import com.eval.global.security.CustomUserDetails;
  
  import lombok.RequiredArgsConstructor;
  
  @Controller
  @RequiredArgsConstructor 
  public class MultiController{ 
	  private final MultiService multiService;
	  
	  @GetMapping("/evaluation/multi")
	  public String multiPage(Model model) {

	      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	      Object principal = auth.getPrincipal();

	      String empNo = null;
	      String position = null;

	      if (principal instanceof CustomUserDetails customUser) {
	          empNo = customUser.getUsername(); // empNo
	          position = customUser.getPosition(); // UI 제어용
	      } else if (principal instanceof UserDetails userDetails) {
	          empNo = userDetails.getUsername();
	      } else if (principal != null) {
	          empNo = principal.toString();
	      }

	      if (empNo == null) {
	          return "redirect:/login";
	      }

	      List<MultiEvalDTO> evalList = multiService.getMultiList(empNo, position);
	      model.addAttribute("evalList", evalList);
	      System.out.println("===== evalList 내용 =====");
	      for (MultiEvalDTO eval : evalList) {
	          System.out.println("evalTypeId=" + eval.getEvalTypeId() +
	                             ", evaluatorNo=" + eval.getEvaluatorNo() +
	                             ", evaluateeNo=" + eval.getEvaluateeNo() +
	                             ", evaluatorName=" + eval.getEvaluatorName() +
	                             ", evaluateeName=" + eval.getEvaluateeName() +
	                             ", position=" + eval.getPosition() +
	                             ", deptName=" + eval.getDeptName() +
	                             ", statusName=" + eval.getStatusName());
	      }

	      model.addAttribute("userPosition", position); // UI에서 필요하면
	      System.out.println("empNo=" + empNo + ", position=" + position + ", evalList.size()=" + evalList.size());
	      
	      return "evaluation/multi/multi";
	  }
	  
	  @GetMapping("/evaluation/multi/detail/{evalTypeId}/{empNo}")
	  @ResponseBody
	  public MultiEvalDTO getMultiEvalDetail(@PathVariable Long evalTypeId,@PathVariable String empNo,Authentication authentication) {
	      CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

	      String loginEmpNo = user.getUsername();
	      String position = user.getPosition();

	      return multiService.getMultiDetail(evalTypeId, empNo, loginEmpNo, position);
	  }

	  @GetMapping("/evaluation/multi/{evalTypeId}/{empNo}/sheet")
	  public String multiSheet(@PathVariable Long evalTypeId, @PathVariable String empNo, Model model, Authentication auth) {
	      
	      // 로그인 정보 가져오기
	      CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
	      String loginEmpNo = user.getUsername();
	      String position = user.getPosition();
	      
	      // DTO 가져오기
	      MultiEvalDTO dto = multiService.getMultiDetail(evalTypeId, empNo, loginEmpNo, position);
	      
	      System.out.println("evalTypeId = " + evalTypeId);
	      System.out.println("empNo = " + empNo);
	      
	      // model에 담기
	      model.addAttribute("eval", dto);
	      
	      return "evaluation/multi/sheet";
	  }
	  
	  @PostMapping("/evaluation/multi/submit")
	  public String submitMulti(@RequestParam Map<String, String> formData,  Authentication auth) {
		  
		  // 로그인 정보에서 평가자 empNo 가져오기
		    CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		    String evaluatorNo = user.getUsername();

		    // evaluateeNo와 evalTypeId는 formData나 hidden input에서 가져와야 함
		    String evaluateeNo = formData.get("evaluateeNo");
		    Integer evalTypeId = Integer.parseInt(formData.get("evalTypeId"));

		  
	      List<MultiEvalAnswer> answers = new ArrayList<>();
	      
	      System.out.println("폼 데이터: " + formData);
	      
	      formData.forEach((key, value) -> {
	          try {
	              MultiEvalAnswer ans = null;
	              Long questionId = null;

	              if (key.startsWith("q")) { // 점수
	                  questionId = Long.parseLong(key.substring(1));
	                  BigDecimal score = new BigDecimal(value);

	                  ans = new MultiEvalAnswer();
	                  ans.setQuestionId(questionId);
	                  ans.setScore(score);

	              } else if (key.startsWith("comment")) { // 의견
	                  questionId = Long.parseLong(key.substring(7));

	                  ans = new MultiEvalAnswer();
	                  ans.setQuestionId(questionId);
	                  ans.setContent(value);
	              }

	              if (ans != null) {
	                  // 공통 필드 설정
	                  ans.setCreatedAt(LocalDateTime.now());

	                  // mappingId 가져오기 (없으면 null 체크)
	                  String mappingKey = "mapping" + questionId;
	                  if (formData.containsKey(mappingKey) && !formData.get(mappingKey).isBlank()) {
	                      ans.setMappingId(Long.parseLong(formData.get(mappingKey)));
	                  } else {
	                      // nullable=false라면 기본값 설정 필요
	                      System.out.println("⚠️ mappingId 없음 for questionId=" + questionId);
	                      ans.setMappingId(0L); // 예: 기본값 0
	                  }

	                  System.out.println("➡ 답변 생성: " + ans);
	                  answers.add(ans);
	              }

	          } catch (NumberFormatException e) {
	              System.out.println("❌ NumberFormatException for key=" + key + ", value=" + value);
	              e.printStackTrace();
	          }
	      });

	      System.out.println("총 저장될 답변 수: " + answers.size());
	      System.out.println("evalTypeId: " + evalTypeId);
	      try {
	    	  multiService.saveAnswers(answers, evaluatorNo, evaluateeNo, evalTypeId);
	          System.out.println("✅ 답변 저장 완료");
	      } catch (Exception e) {
	          System.out.println("❌ 답변 저장 실패: " + e.getMessage());
	          e.printStackTrace();
	      }

	      return "redirect:/evaluation/multi"; // 필요에 따라 리다이렉트 URL 변경
	  }



  }
