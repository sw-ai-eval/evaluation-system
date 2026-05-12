/*
 * package com.eval.domain.multi.controller;
 * 
 * import java.util.List;
 * 
 * import org.springframework.stereotype.Controller; import
 * org.springframework.ui.Model; import
 * org.springframework.web.bind.annotation.GetMapping; import
 * org.springframework.security.core.annotation.AuthenticationPrincipal;
 * 
 * import com.eval.domain.multi.dto.MultiEvalDTO; import
 * com.eval.domain.multi.service.MultiService; import
 * com.eval.global.security.CustomUserDetails;
 * 
 * import lombok.RequiredArgsConstructor;
 * 
 * @Controller
 * 
 * @RequiredArgsConstructor public class MultiController{ private final
 * MultiService multiService;
 * 
 * @GetMapping("/evaluation/multi") public String multiPage(Model
 * model, @AuthenticationPrincipal CustomUserDetails user) {
 * 
 * String userNo = user.getUsername(); String position = user.getPosition();
 * 
 * List<MultiEvalDTO> evalList = multiService.getMultiList(userNo, position);
 * model.addAttribute("evalList", evalList);
 * 
 * return "evaluation/multi"; }
 * 
 * }
 */