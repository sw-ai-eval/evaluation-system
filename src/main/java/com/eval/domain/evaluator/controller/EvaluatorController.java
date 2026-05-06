package com.eval.domain.evaluator.controller;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.service.DepartmentService;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.service.EmployeeService;
import com.eval.domain.evaluator.serivce.EvaluatorService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EvaluatorController {

    private final DepartmentService departmentService;
    private final EvaluatorService evaluatorService;
    //private final EmployeeService employeeService;

    @GetMapping("/evaluator")
    public String evaluators(Model model,
                             @RequestParam(required = false) String deptId,
                             @ModelAttribute("errorMessage") String errorMessage) {

        List<Department> deptList = departmentService.findDepartmentUse();
        
        
        if (deptId != null && !deptId.isBlank()) {
            model.addAttribute("evalList", evaluatorService.getEvaluatorList(deptId));
        } else {
            model.addAttribute("evalList", List.of());
        }

        model.addAttribute("deptList", deptList);
        model.addAttribute("selectedDept", deptId);

        model.addAttribute("errorMessage", errorMessage != null ? errorMessage : null);
        
        model.addAttribute("pageType", "evaluator/evaluator_mapping");
        return "evaluator/evaluator_mapping";
    }

    @PostMapping("/evaluator/create")
    public String createEvaluator(@RequestParam String deptId,
                                  RedirectAttributes rttr) {

        try {
            evaluatorService.createEvaluatorMapping(deptId);
        } catch (Exception e) {   
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/evaluator?deptId=" + deptId;
    }
    


}
