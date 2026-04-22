package com.eval.domain.dept.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dept")
public class DeptController {

    // 1. 부서 등록 폼 (http://localhost:8080/dept/register)
    @GetMapping("/register")
    public String registerForm() {
        return "employee/dept-register"; 
    }

    // 2. 부서 목록 조회 (http://localhost:8080/dept/list)
    @GetMapping("/list")
    public String listPage() {
        return "employee/dept-list";
    }
}