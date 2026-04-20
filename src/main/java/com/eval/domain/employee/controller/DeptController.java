package com.eval.domain.employee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dept") // 관리자 전용 경로 설정
public class DeptController {

    @GetMapping("/list")
    public String deptListPage() {
        // templates/employee/dept_list.html 호출
        return "employee/dept_list";
    }
}