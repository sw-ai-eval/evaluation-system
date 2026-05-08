package com.eval.domain.evaluator.controller;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.service.DepartmentService;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.service.EmployeeService;
import com.eval.domain.evaluator.dto.EvaluatorDetailDto;
import com.eval.domain.evaluator.dto.EvaluatorUpdateRequest;
import com.eval.domain.evaluator.dto.EvaluatorVeiwDto;
import com.eval.domain.evaluator.service.EvaluatorService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EvaluatorController {

    private final DepartmentService departmentService;
    private final EvaluatorService evaluatorService;
    private final EmployeeService employeeService;

    @GetMapping("/evaluator")
    public String evaluators(Model model,
                             @RequestParam(required = false) String deptId,
                             @ModelAttribute("errorMessage") String errorMessage) {

        List<Department> deptList = departmentService.findDepartmentUse();
        List<Employee> executiveList = employeeService.findExecutive();

        if (deptId != null && !deptId.isBlank()) {
            model.addAttribute("evalList", evaluatorService.getEvaluatorList(deptId));
        } else {
            model.addAttribute("evalList", List.of());
        }
        
        model.addAttribute("deptList", deptList);
        model.addAttribute("selectedDept", deptId);
        model.addAttribute("executiveList",executiveList);
        
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
    
    @PostMapping("/evaluator/reset")
    public String resetEvaluator(@RequestParam String deptId,  RedirectAttributes rttr) {

        try {
        	evaluatorService.resetEvaluatorMapping(deptId);
        } catch (Exception e) {   
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }
    	return "redirect:/evaluator?deptId=" + deptId;
    }
    
    @GetMapping("/evaluator/detail/{empNo}")
    @ResponseBody
    public EvaluatorDetailDto detail(@PathVariable String empNo) {
        return evaluatorService.getDetail(empNo);
    }
    
    @PostMapping("/evaluator/update")
    public String updateEvaluator(@ModelAttribute EvaluatorUpdateRequest request) {

        evaluatorService.update(request);

        return "redirect:/evaluator?deptId=" + request.getDeptId();
    }
    
	/*
	 * @GetMapping("/evaluator/search") public String
	 * searchEvaluatee(@RequestParam(required = false) String empNo) {
	 * 
	 * 
	 * return "redirect:/evaluator?empNo=" + empNo; }
	 */

}
