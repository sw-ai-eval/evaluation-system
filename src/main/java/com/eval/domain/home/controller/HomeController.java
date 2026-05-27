package com.eval.domain.home.controller;

import com.eval.domain.employee.dto.EmpManageDTO;
import com.eval.domain.employee.mapper.EmployeeMapper;
import com.eval.domain.home.Notice;
import com.eval.domain.home.dto.NoticeDetailDto;
import com.eval.domain.home.dto.NoticeDto;
import com.eval.domain.home.dto.NoticeListDto;
import com.eval.domain.home.dto.TodoListDto;
import com.eval.domain.home.mapper.HomeMapper;
import com.eval.domain.home.service.HomeService;
import com.eval.domain.interview.mapper.InterviewMapper;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EmployeeMapper employeeMapper;
    private final InterviewMapper interviewMapper;
    private final HomeService homeService;
    private final HomeMapper homeMapper;

    @GetMapping("/home")
    public String homePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        
        if (userDetails != null) {
            String empNo = userDetails.getUsername(); 
            
            EmpManageDTO employee = employeeMapper.findByEmpNoDetail(empNo);
            Long interviewCount = interviewMapper.countOngoingInterviewsByEmpNo(empNo);
            List<TodoListDto> todoList = homeService.getTodoList(empNo);
            List<NoticeListDto> noticeList= homeMapper.selectNoticeList();
            double myEvalPercent = homeService.getMyOngoingEvalPercent(empNo);
            double AllEvalPercent = homeService.getDeptOngoingEvalPercent();
            long inCompleteEmpNum = homeMapper.countAllNotStartedEvalEmpNum();
            
            if (employee != null) {
                model.addAttribute("empName", employee.getName());
                model.addAttribute("empNo", employee.getEmpNo());
                model.addAttribute("deptName", employee.getDeptName());
                model.addAttribute("position", employee.getLevelName());
                model.addAttribute("interviewCount", interviewCount);
                model.addAttribute("todoList", todoList);
                model.addAttribute("noticeList", noticeList);
                model.addAttribute("myEvalPercent", myEvalPercent);
                model.addAttribute("AllEvalPercent", AllEvalPercent);
                model.addAttribute("inCompleteEmpNum", inCompleteEmpNum);
            }
        }
        
        return "home/home"; 
    }
    
    @GetMapping("/admin/notice/write")
    public String writeNotice(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        
        if (userDetails != null) {
            String empNo = userDetails.getUsername(); 
            
            EmpManageDTO employee = employeeMapper.findByEmpNoDetail(empNo);
     
            if (employee != null) {
                model.addAttribute("empNo", employee.getEmpNo());

            }
        }
        
        return "home/notice/write"; 
        
    }
    
    @PostMapping("/admin/notice/save")
    public String noticeSave(@ModelAttribute NoticeDto dto) {

        homeService.saveNotice(dto);

        return "redirect:/notice/list";
    }
    
    @GetMapping("/notice/list")
    public String listNoticePage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {

        String empNo = (userDetails != null) ? userDetails.getUsername() : null;

        EmpManageDTO employee = (empNo != null)
                ? employeeMapper.findByEmpNoDetail(empNo)
                : null;

        Page<NoticeListDto> noticePage = homeService.getNoticePage(title, page, size);

        if (employee != null) {
            model.addAttribute("empNo", employee.getEmpNo());
        }

        model.addAttribute("noticePage", noticePage);
        model.addAttribute("title", title);

        return "home/notice/list";
    }
    
    @GetMapping("/notice/{id}")
    public String noticeDetail(@PathVariable Integer id, Model model) {

        NoticeDetailDto notice = homeService.getNoticeDetail(id);

        model.addAttribute("notice", notice);

        return "home/notice/detail";
    }
    
    @PostMapping("/admin/notice/delete")
    @ResponseBody
    public ResponseEntity<?> deleteNotice(@RequestBody Map<String, Integer> req) {

        Integer id = req.get("id");

        homeService.deleteNotice(id);

        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/admin/notice/update")
    @ResponseBody
    public ResponseEntity<?> updateNotice(@RequestBody NoticeDto dto, @AuthenticationPrincipal UserDetails userDetails) {
    	String empNo = userDetails.getUsername(); 
    	
        homeService.updateNotice(dto, empNo);
        return ResponseEntity.ok().build();
    }
    
  
}