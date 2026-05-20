package com.eval.domain.interview.controller;

import com.eval.domain.codetable.CodeService;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.service.EmployeeService;
import com.eval.domain.interview.dto.InterviewListDto;
import com.eval.domain.interview.dto.InterviewTypeNameDto;
import com.eval.domain.interview.dto.InterviewTypeNameDto.InterviewCategoryLabels;
import com.eval.domain.interview.service.InterviewService;
import com.eval.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final EmployeeService employeeService;
    private final CodeService codeService;

    /**
     * 면담 페이지 로딩
     */
    @GetMapping("/interview")
    public String interviews(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        String empNo = null;
        String position = null;

        if (principal instanceof CustomUserDetails customUser) {
            empNo = customUser.getUsername();
            position = customUser.getPosition();
        } else if (principal instanceof UserDetails userDetails) {
            empNo = userDetails.getUsername();
        } else if (principal != null) {
            empNo = principal.toString();
        }

        if (empNo == null) {
            return "redirect:/login";
        }

        // DB에서 필요한 데이터 조회
        List<Employee> employeeList = employeeService.getActiveColleaguesByEmpNoAndPosition(empNo, "부서원");
        List<InterviewListDto> interviewList = interviewService.getInterviewList(empNo, position);
        List<InterviewTypeNameDto> interviewTypeList = interviewService.getAllInterviewTypes();
        
        

        // Model에 데이터 담기
        model.addAttribute("employeeList", employeeList);
        model.addAttribute("interviewList", interviewList);
        model.addAttribute("interviewTypeList", interviewTypeList);
        model.addAttribute("userPosition", position);
        model.addAttribute("statusList", codeService.getInterviewStatusList());
        
        model.addAttribute("pageType", "interview/interview");

        return "interview/interview";
    }

    /**
     * 특정 타입의 카테고리 라벨 반환
     */
    @GetMapping("/interview/labels")
    @ResponseBody
    public List<InterviewCategoryLabels> getLabelsByType(@RequestParam Long typeId) {
        InterviewCategoryLabels labels = interviewService.getLabelsForType(typeId);
        return List.of(labels); // 프론트에서 배열 형태로 받도록 처리
    }
} 