
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.eval.domain.multi.MultiEvalAnswer;
import com.eval.domain.multi.dto.MultiEvalDTO;
import com.eval.domain.multi.dto.PageResponse;
import com.eval.domain.multi.mapper.MultiMapper;
import com.eval.domain.multi.service.MultiService;
  import com.eval.global.security.CustomUserDetails;
  
  import lombok.RequiredArgsConstructor;
  
  @Controller
  @RequiredArgsConstructor 
  public class MultiController{ 
	  private final MultiService multiService;
	  
	  @GetMapping("/evaluation/multi")
	  public String multiPage(Model model,
	                          @RequestParam(required = false) Integer year,
	                          @RequestParam(required = false) String period,
	                          @RequestParam(defaultValue = "progress") String tab,
	                          @RequestParam(defaultValue = "10") int size) {

	      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	      Object principal = auth.getPrincipal();

	      String empNo = null;
	      String position = null;
	      String role = null;

	      if (principal instanceof CustomUserDetails customUser) {
	          empNo = customUser.getUsername();
	          position = customUser.getPosition();
	          role = customUser.getRole();
	      } else if (principal instanceof UserDetails userDetails) {
	          empNo = userDetails.getUsername();
	      } else {
	          empNo = principal.toString();
	      }

	      if (empNo == null) {
	          return "redirect:/login";
	      }

	      // 👉 여기서 리스트 조회 ❌ 제거 (중요)

	      model.addAttribute("selectedYear", year);
	      model.addAttribute("selectedPeriod", period);
	      model.addAttribute("userPosition", position);

	      model.addAttribute("tab", tab);

	      List<Integer> yearList = multiService.getAvailableYears();
	      model.addAttribute("yearList", yearList);

	      return "evaluation/multi/multi";
	  }
	  
	  
	  @GetMapping("/evaluation/multi/detail/{evalTypeId}/{evaluatorNo}/{evaluateeNo}")
	  @ResponseBody
	  public MultiEvalDTO getMultiEvalDetail(@PathVariable Long evalTypeId,@PathVariable String evaluatorNo,@PathVariable String evaluateeNo, Authentication authentication) {
	      CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

	      String loginEmpNo = user.getUsername();
	      String position = user.getPosition();

	      return multiService.getMultiDetail(evalTypeId, evaluatorNo, evaluateeNo,  position);
	  }

	  @GetMapping("/evaluation/multi/{evalTypeId}/{evaluatorNo}/{evaluateeNo}/sheet")
	  public String multiSheet(@PathVariable Long evalTypeId, @PathVariable String evaluatorNo, @PathVariable String evaluateeNo, 
			  Model model, Authentication auth, RedirectAttributes redirectAttributes) {
	      
	      // 로그인 정보 가져오기
	      CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
	      String loginEmpNo = user.getUsername();
	      String position = user.getPosition();
	      
	      if(multiService.ifFinishSelfEval(loginEmpNo)) {
	    	  redirectAttributes.addFlashAttribute(
	                  "alertMessage",
	                  "성과평가, 역량평가의 본인 평가를 모두 완료해야 부서장의 다면평가를 진행할 수 있습니다!"
	          );
	    	  
	    	  return "redirect:/evaluation/multi";
	      }
	      
	      // DTO 가져오기
	      MultiEvalDTO dto = multiService.getMultiDetail(evalTypeId, evaluatorNo, evaluateeNo, position);
	      
	      System.out.println("evalTypeId = " + evalTypeId);
	      System.out.println("evaluatorNo = " + evaluatorNo);
	      System.out.println("evaluateeNo = " + evaluateeNo);
	      
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
