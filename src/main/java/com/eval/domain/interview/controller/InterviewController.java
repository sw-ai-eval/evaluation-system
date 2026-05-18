package com.eval.domain.interview.controller;

import com.eval.domain.interview.Interview;
import com.eval.domain.interview.dto.InterviewListDto;
import com.eval.domain.interview.service.InterviewService;
import com.eval.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/interview")
    public String interviews(Model model) {
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

        List<InterviewListDto> list = interviewService.getInterviewList(empNo, position);

        model.addAttribute("interviewList", list);


        model.addAttribute("userPosition", position);
        model.addAttribute("pageType", "interview/interview");
        return "interview/interview";
    }



}
