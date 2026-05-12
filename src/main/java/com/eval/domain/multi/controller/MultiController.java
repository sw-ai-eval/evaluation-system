
package com.eval.domain.multi.controller;
  
import java.util.List;
  
 import org.springframework.stereotype.Controller; 
 import org.springframework.ui.Model; 
 import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
	      model.addAttribute("userPosition", position); // UI에서 필요하면
	      System.out.println("empNo=" + empNo + ", position=" + position + ", evalList.size()=" + evalList.size());
	      
	      return "evaluation/multi";
	  }
	  
	  @GetMapping("/evaluation/multi/detail/{typeId}/{empNo}")
	  @ResponseBody
	  public MultiEvalDTO getMultiEvalDetail(@PathVariable Long typeId,
	                                         @PathVariable String empNo,
	                                         Authentication authentication) {
	      CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

	      String loginEmpNo = user.getUsername();
	      String position = user.getPosition();

	      return multiService.getMultiDetail(typeId, empNo, loginEmpNo, position);
	  }

  }
