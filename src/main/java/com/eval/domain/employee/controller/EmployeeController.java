package com.eval.domain.employee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee") 
public class EmployeeController {

    @GetMapping("/dept-register")
    public String deptRegisterPage() {
        return "employee/dept-register"; 
    }
}