package com.eval.domain.evaluator.controller;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.service.DepartmentService;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.service.EmployeeService;
import com.eval.domain.evaluation.EvalType;
import com.eval.domain.evaluation.service.EvaluationService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class EvaluatorController {

    private final DepartmentService departmentService;
    private final EvaluatorService evaluatorService;
    private final EmployeeService employeeService;
    private final EvaluationService evaluationService;

    @GetMapping("/evaluator")
    public String evaluators(Model model,
                             @RequestParam(required = false) String deptId,
                             @RequestParam(required = false) Integer typeId,
                             @RequestParam(required = false) String employeeSearch, 
                             @ModelAttribute("errorMessage") String errorMessage) {

        List<Department> deptList = departmentService.findDepartmentUse();
        List<Employee> executiveList = employeeService.findExecutive();

        if (deptId != null && !deptId.isBlank()) {
            model.addAttribute("evalList", evaluatorService.getEvaluatorList(deptId, typeId, employeeSearch));
        } else {
            model.addAttribute("evalList", List.of());
        }
        
        List<EvalType> evalTypeList = evaluationService.findAll();

        model.addAttribute("evalTypeList", evalTypeList);
        model.addAttribute("selectedTypeId", typeId);
        
        model.addAttribute("deptList", deptList);
        model.addAttribute("selectedDept", deptId);
        model.addAttribute("executiveList",executiveList);
        
        model.addAttribute("errorMessage", errorMessage != null ? errorMessage : null);
        
        model.addAttribute("pageType", "evaluator/evaluator_mapping");
        return "evaluator/evaluator_mapping";
    }

    @PostMapping("/evaluator/create")
    public String createEvaluator(@RequestParam String deptId, @RequestParam Integer typeId,
                                  RedirectAttributes rttr) {

        try {
            evaluatorService.createEvaluatorMapping(deptId, typeId);
        } catch (Exception e) {   
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/evaluator?deptId=" + deptId + "&typeId=" + typeId;
    }
    
    @PostMapping("/evaluator/reset")
    public String resetEvaluator(@RequestParam String deptId,  @RequestParam Integer typeId, RedirectAttributes rttr) {

        try {
        	evaluatorService.resetEvaluatorMapping(deptId, typeId);
        } catch (Exception e) {   
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/evaluator?deptId=" + deptId + "&typeId=" + typeId;
    }
    
    @GetMapping("/evaluator/detail/{empNo}")
    @ResponseBody
    public EvaluatorDetailDto detail(
            @PathVariable String empNo,
            @RequestParam Integer typeId) {

        return evaluatorService.getDetail(empNo, typeId);
    }
    
    @PostMapping("/evaluator/update")
    public String updateEvaluator(@ModelAttribute EvaluatorUpdateRequest request, @RequestParam Integer typeId) {

        evaluatorService.update(request, typeId);

        return "redirect:/evaluator?deptId=" +
        request.getDeptId() +
        "&typeId=" +
        request.getTypeId();
    }
    
    @PostMapping("/evaluator/delete")
    @ResponseBody
    public Map<String, Object> deleteEvaluator(
            @RequestParam String deptId,
            @RequestParam String evaluateeNo,
            @RequestParam Integer typeId) {

        Map<String, Object> result = new HashMap<>();

        try {
            evaluatorService.delete(deptId, evaluateeNo, typeId);
            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

}
