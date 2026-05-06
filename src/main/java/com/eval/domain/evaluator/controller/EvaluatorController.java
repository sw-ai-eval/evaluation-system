package com.eval.domain.evaluator.controller;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.service.DepartmentService;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EvaluatorController {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    @GetMapping("/evaluator")
    public String evaluators(Model model,
                             @RequestParam(required = false) String deptId,
                             @RequestParam(required = false) String emp) {

        // 1️⃣ 부서 리스트 (항상 내려줌)
        List<Department> deptList = departmentService.findDepartmentUse();

        // 2️⃣ 사원 리스트 (조건 검색)
        List<Employee> empList;


        model.addAttribute("deptList", deptList);
//        model.addAttribute("empList", empList);

        // 선택값 유지 (UX 중요)
        model.addAttribute("selectedDept", deptId);
//        model.addAttribute("searchEmp", emp);

        model.addAttribute("pageType", "evaluator/evaluator_mapping");
        return "evaluator/evaluator_mapping";
    }

    @PostMapping("/evaluator/create")
    public String createEvaluator(){


        return "/redirect:/evaluator";
    }


}
