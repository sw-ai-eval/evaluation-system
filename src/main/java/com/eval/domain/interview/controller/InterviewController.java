package com.eval.domain.interview.controller;

import com.eval.domain.codetable.CodeCommonRepository;
import com.eval.domain.codetable.CodeService;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.employee.service.EmployeeService;
import com.eval.domain.interview.Interview;
import com.eval.domain.interview.InterviewTopicMapping;
import com.eval.domain.interview.dto.InterviewDetailDto;
import com.eval.domain.interview.dto.InterviewListDto;
import com.eval.domain.interview.dto.InterviewSaveRequestDto;
import com.eval.domain.interview.dto.InterviewTypeNameDto;
import com.eval.domain.interview.dto.InterviewTypeNameDto.InterviewCategoryLabels;
import com.eval.domain.interview.repository.InterviewRepository;
import com.eval.domain.interview.service.InterviewService;
import com.eval.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final EmployeeService employeeService;
    private final CodeService codeService;
    private final InterviewRepository interviewRepository;
    private final EmployeeRepository employeeRepository;
    private final CodeCommonRepository codeCommonRepository;

    
    @GetMapping("/interview")
    public String interviews(Model model, 
    		@RequestParam(required = false) String startDay,
            @RequestParam(required = false) String employee,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "7") int size ) {
    	
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        String empNo = null;
        String position = null;
        String role=null;

        if (principal instanceof CustomUserDetails customUser) {
            empNo = customUser.getUsername();
            position = customUser.getPosition();
            role =customUser.getRole();
        } else if (principal instanceof UserDetails userDetails) {
            empNo = userDetails.getUsername();
        } else if (principal != null) {
            empNo = principal.toString();
        }

        if (empNo == null) {
            return "redirect:/login";
        }

        List<Employee> employeeList = employeeService.getActiveColleaguesByEmpNoAndPosition(empNo, "부서원");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").descending());

        Page<InterviewListDto> interviewPage =interviewService.getInterviewPage(empNo, position, startDay, employee, type, status, pageable,role);
        
        List<InterviewTypeNameDto> interviewTypeList = interviewService.getAllInterviewTypes();

        model.addAttribute("employeeList", employeeList);
        model.addAttribute("interviewPage", interviewPage);
        model.addAttribute("interviewTypeList", interviewTypeList);
        model.addAttribute("userPosition", position);
        model.addAttribute("statusList", codeService.getInterviewStatusList());
        
        model.addAttribute("pageType", "interview/interview");

        return "interview/interview";
    }


    //특정 타입의 카테고리 라벨 반환
    @GetMapping("/interview/labels")
    @ResponseBody
    public List<InterviewCategoryLabels> getLabelsByType(@RequestParam Long typeId) {
        InterviewCategoryLabels labels = interviewService.getLabelsForType(typeId);
        return List.of(labels); 
    }
    
    //저장
    @PostMapping("/api/interview/save")
    @ResponseBody
    public ResponseEntity<String> saveInterviewSchedule(
            @RequestBody InterviewSaveRequestDto request,
            Authentication authentication) {

        CustomUserDetails user =(CustomUserDetails) authentication.getPrincipal();

        String loginEmpNo = user.getUsername();

        interviewService.saveInterview(request, loginEmpNo);

        return ResponseEntity.ok("success");
    }
    
    @GetMapping("/interview/detail")
    public ResponseEntity<InterviewDetailDto> getDetail(@RequestParam Long id) {

        Optional<Interview> interviewOpt = interviewRepository.findById(id);
        if (interviewOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Interview interview = interviewOpt.get();

        List<String> topics = interview.getTopics() == null
                ? new ArrayList<>()
                : interview.getTopics().stream()
                           .map(InterviewTopicMapping::getTopicName)
                           .toList();

        String evaluateeName = employeeRepository.findNameByEmpNo(interview.getEvaluateeNo());

        InterviewDetailDto dto = new InterviewDetailDto(
                interview.getId(),
                interview.getStartAt(),
                interview.getEndAt(),
                interview.getType(),
                interview.getEvaluateeNo(),
                evaluateeName,
                topics,
                interview.getPlace(),
                interview.getStatus(),
                interview.getDetail()
        );

        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/api/interview/delete")
    @ResponseBody
    public void deleteInterviewSchedule( @RequestBody InterviewSaveRequestDto request) {

        interviewService.deleteInterview(request);

    }
    
    
    
} 