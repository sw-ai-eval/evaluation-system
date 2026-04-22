package com.eval.domain.admin.controller;

import com.eval.domain.employee.dto.EmployeeDTO;
import com.eval.domain.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
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
    public String adminMain(Model model) {
        // DB에서 전체 사원 목록을 가져와서 'employees'라는 이름으로 화면에 전달
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
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