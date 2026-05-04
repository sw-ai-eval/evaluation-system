package com.eval.domain.admin.controller;

import com.eval.domain.employee.dto.EmployeeDTO;
import com.eval.domain.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeService employeeService;

    @GetMapping("/management")
    public String adminMain(
        @RequestParam(value = "page", defaultValue = "0") int page,
        Model model) {
        
        List<EmployeeDTO> allEmployees = employeeService.getAllEmployees();

        int pageSize = 10;
        int start = page * pageSize;
        int end = Math.min((start + pageSize), allEmployees.size());
        
        if (start > allEmployees.size()) {
            start = allEmployees.size();
        }
        
        List<EmployeeDTO> pagedList = allEmployees.subList(start, end);
        Page<EmployeeDTO> employeePage = new PageImpl<>(pagedList, PageRequest.of(page, pageSize), allEmployees.size());

        model.addAttribute("employees", employeePage);
        
        return "admin/management";
    }
    
    @PostMapping("/reset-password")
    @ResponseBody
    public String resetPassword(@RequestParam("targetEmpNo") String targetEmpNo) {
        try {
            employeeService.resetEmployeePassword(targetEmpNo);
            return "success"; 
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}