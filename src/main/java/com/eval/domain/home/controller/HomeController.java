package com.eval.domain.home.controller;

import com.eval.domain.employee.dto.EmpManageDTO;
import com.eval.domain.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EmployeeMapper employeeMapper;

    @GetMapping("/home")
    public String homePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        
        if (userDetails != null) {
            String empNo = userDetails.getUsername(); 
            
            EmpManageDTO employee = employeeMapper.findByEmpNoDetail(empNo);
            
            if (employee != null) {
                model.addAttribute("empName", employee.getName());
                model.addAttribute("empNo", employee.getEmpNo());
                model.addAttribute("deptName", employee.getDeptName());
                model.addAttribute("position", employee.getPositionLevel());
            }
        }
        
        return "home/home"; 
    }
}