
package com.eval.domain.multi.controller;
  
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

	      List<MultiEvalDTO> inProgressList = multiService.getMultiProgressList(empNo, position);
	      List<MultiEvalDTO> completedList = multiService.getMultiCompletedList(empNo, position);
	      model.addAttribute("inProgressList", inProgressList);
	      model.addAttribute("completedList", completedList);
	      
	      System.out.println("===== evalList 내용 =====");
	      for (MultiEvalDTO eval : inProgressList) {
	          System.out.println("evalTypeId=" + eval.getEvalTypeId() +
	                             ", evaluatorNo=" + eval.getEvaluatorNo() +
	                             ", evaluateeNo=" + eval.getEvaluateeNo() +
	                             ", evaluatorName=" + eval.getEvaluatorName() +
	                             ", evaluateeName=" + eval.getEvaluateeName() +
	                             ", position=" + eval.getPosition() +
	                             ", deptName=" + eval.getDeptName() +
	                             ", statusName=" + eval.getStatusName());
	      }
	      
	      for (MultiEvalDTO eval : completedList) {
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
	      System.out.println("empNo=" + empNo + ", position=" + position + ", evalList.size()=" + inProgressList.size());
	      System.out.println("empNo=" + empNo + ", position=" + position + ", evalList.size()=" + completedList.size());
	      
	      return "evaluation/multi/multi";
	  }
	  
	  @GetMapping("/evaluation/multi/detail/{evalTypeId}/{evaluatorNo}/{empNo}")
	  @ResponseBody
	  public MultiEvalDTO getMultiEvalDetail(@PathVariable Long evalTypeId,@PathVariable String evaluatorNo,@PathVariable String empNo, Authentication authentication) {
	      CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

	      String loginEmpNo = user.getUsername();
	      String position = user.getPosition();

	      return multiService.getMultiDetail(evalTypeId, empNo, evaluatorNo, position);
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
	  public String submitMulti(@RequestParam Map<String, String> formData, Authentication auth) {

	      CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

	      String evaluatorNo = user.getUsername();

	      String evaluateeNo = formData.get("evaluateeNo");

	      Integer evalTypeId = Integer.parseInt(formData.get("evalTypeId"));

	      List<MultiEvalAnswer> answers = parseAnswers(formData);

	      multiService.saveAnswers( answers, evaluatorNo, evaluateeNo, evalTypeId);
	      multiService.saveSummary(evaluateeNo, evalTypeId);

	      return "redirect:/evaluation/multi";
	  }

	  @PostMapping("/evaluation/multi/temporary")
	  public String temporaryMulti(@RequestParam Map<String, String> formData,Authentication auth) {

	      CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

	      String evaluatorNo = user.getUsername();

	      String evaluateeNo = formData.get("evaluateeNo");

	      Integer evalTypeId =Integer.parseInt(formData.get("evalTypeId"));

	      List<MultiEvalAnswer> answers = parseAnswers(formData);

	      multiService.temporarySaveAnswers(answers, evaluatorNo,evaluateeNo, evalTypeId);

	      return "redirect:/evaluation/multi";
	  }
	  
	  private List<MultiEvalAnswer> parseAnswers(Map<String, String> formData) {

		    Map<Long, MultiEvalAnswer> answerMap = new HashMap<>();

		    formData.forEach((key, value) -> {

		        try {

		            Long questionId = null;

		            if (key.startsWith("q")) {
		                questionId = Long.parseLong(key.substring(1));

		            } else if (key.startsWith("comment")) {
		                questionId = Long.parseLong(key.substring(7));

		            } else if (key.startsWith("mapping")) {
		                questionId = Long.parseLong(key.substring(7));
		            }

		            if (questionId == null) return;

		            MultiEvalAnswer ans =
		                    answerMap.getOrDefault(questionId, new MultiEvalAnswer());

		            ans.setQuestionId(questionId);

		            if (key.startsWith("q")) {
		                ans.setScore(new BigDecimal(value));
		            }

		            if (key.startsWith("comment")) {
		                ans.setContent(value);
		            }

		            if (key.startsWith("mapping")) {
		                ans.setMappingId(Long.parseLong(value));
		            }

		            ans.setCreatedAt(LocalDateTime.now());

		            answerMap.put(questionId, ans);

		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    });

		    return new ArrayList<>(answerMap.values());
		}
  }
