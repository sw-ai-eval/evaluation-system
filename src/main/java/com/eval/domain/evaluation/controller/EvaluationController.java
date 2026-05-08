package com.eval.domain.evaluation.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.service.EmployeeService;
import com.eval.domain.evaluation.DeptEvalGrade;
import com.eval.domain.evaluation.DeptEvalWeight;
import com.eval.domain.evaluation.EvalItem;
import com.eval.domain.evaluation.EvalType;
import com.eval.domain.evaluation.repository.EvalItemRepository;
import com.eval.domain.evaluation.repository.EvalTypeRepository;
import com.eval.domain.evaluation.service.EvaluationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/evaluation")
public class EvaluationController {

    private final DepartmentRepository departmentRepository;
    private final EvaluationService evaluationService;
    private final EvalTypeRepository evalTypeRepository;
    private final EvalItemRepository evalItemRepository;
    private final EmployeeService employeeService;

    public EvaluationController(DepartmentRepository departmentRepository, 
                                EvaluationService evaluationService,
                                EvalTypeRepository evalTypeRepository,
                                EvalItemRepository evalItemRepository,
                                EmployeeService employeeService) {
        this.departmentRepository = departmentRepository;
        this.evaluationService = evaluationService;
        this.evalTypeRepository = evalTypeRepository;
        this.evalItemRepository = evalItemRepository;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String evaluationSetting(
            @RequestParam(value = "typePage", defaultValue = "0") int typePageNum,
            @RequestParam(value = "itemPage", defaultValue = "0") int itemPageNum,
            Model model) {
        
        List<Department> deptList = departmentRepository.findAll();
        model.addAttribute("deptList", deptList);
        
        List<EvalType> typeList = evalTypeRepository.findAll().stream()
                                    .filter(EvalType::isStatus)
                                    .collect(Collectors.toList());
        model.addAttribute("typeList", typeList);
        
        List<EvalType> weightTypes = evalTypeRepository.findByStatusTrueAndHasWeightTrue();
        model.addAttribute("weightTypes", weightTypes);
        
        int pageSize = 10;
        PageRequest typePageReq = PageRequest.of(typePageNum, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<EvalType> typePage = evalTypeRepository.findAll(typePageReq);
        model.addAttribute("typePage", typePage);
        
        PageRequest itemPageReq = PageRequest.of(itemPageNum, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<EvalItem> itemPage = evalItemRepository.findAll(itemPageReq);
        model.addAttribute("itemPage", itemPage);
        
        return "evaluation/setting";
    }

    @GetMapping("/employees/{deptId}")
    @ResponseBody
    public List<Map<String, Object>> getEmployeesByDept(@PathVariable("deptId") String deptId) {
        List<Employee> empList = employeeService.getEmployeesByDept(deptId);
        
        return empList.stream().map(emp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("empNo", emp.getEmpNo());
            map.put("name", emp.getName());
            return map;
        }).collect(Collectors.toList());
    }

    
    @GetMapping("/items/{itemId}/targets")
    @ResponseBody
    public List<Map<String, Object>> getItemTargets(@PathVariable("itemId") Integer itemId) {
        return evaluationService.getItemTargets(itemId);
    }

    @PostMapping("/save-item")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public String saveEvalItem(@RequestBody Map<String, Object> params) {
        try {
            EvalItem item = new EvalItem();
            if (params.get("id") != null && !params.get("id").toString().isEmpty()) {
                item = evalItemRepository.findById(Integer.parseInt(params.get("id").toString())).orElseThrow();
            }

            Integer typeId = Integer.parseInt(params.get("typeId").toString());
            EvalType type = evalTypeRepository.findById(typeId).orElseThrow();
            
            item.setEvalType(type);
            item.setCategory(params.get("category").toString());
            item.setContent(params.get("content").toString()); 
            item.setAnswerType(params.get("answerType").toString()); 
            item.setCommon("Y".equals(params.get("isCommon").toString()));
            item.setWeight(params.get("weight") != null ? Integer.parseInt(params.get("weight").toString()) : 0);
            
            if (params.get("explanation") != null && !params.get("explanation").toString().trim().isEmpty()) {
                item.setExplanation(params.get("explanation").toString().trim());
            } else {
                item.setExplanation(params.get("category").toString() + " 항목에 대한 세부 평가 문항입니다.");
            }

            if (item.getId() == null) item.setCreatedBy("ADMIN");
            else { item.setUpdatedBy("ADMIN"); item.setUpdatedAt(LocalDateTime.now()); }

            item = evalItemRepository.save(item);
            
            if (!item.isCommon() && params.get("targets") != null) {
                List<Map<String, String>> targets = (List<Map<String, String>>) params.get("targets");
                evaluationService.saveItemTargets(item.getId(), targets);
            } else {
                evaluationService.saveItemTargets(item.getId(), null);
            }
            return "success";
        } catch (Exception e) {
            log.error("❌ 문항 저장 오류: ", e);
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/save-type")
    @ResponseBody
    public String saveEvalType(@RequestBody EvalType evalType) {
        try {
            if (evalType.getId() != null) {
                EvalType existing = evalTypeRepository.findById(evalType.getId()).orElseThrow();
                existing.setName(evalType.getName());
                existing.setYear(evalType.getYear());
                existing.setStartDate(evalType.getStartDate());
                existing.setEndDate(evalType.getEndDate());
                existing.setStatus(evalType.isStatus());
                existing.setGuideline(evalType.getGuideline());
                existing.setHasWeight(evalType.isHasWeight());
                existing.setUpdatedBy("ADMIN");
                existing.setUpdatedAt(LocalDateTime.now());
                evalTypeRepository.save(existing);
            } else {
                evalType.setCreatedBy("ADMIN");
                evalType.setCreatedAt(LocalDateTime.now());
                evalTypeRepository.save(evalType);
            }
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/delete-item/{id}")
    @ResponseBody
    public String deleteEvalItem(@PathVariable("id") Integer id) {
        evalItemRepository.deleteById(id);
        return "success";
    }

    @PostMapping("/save-weights")
    @ResponseBody
    public String saveWeights(@RequestBody List<DeptEvalWeight> weights, @RequestParam(defaultValue = "false") boolean applyToChildren) {
        evaluationService.saveDeptWeights(weights.get(0).getDeptId(), weights, applyToChildren);
        return "success";
    }

    @GetMapping("/grades/{deptId}")
    @ResponseBody
    public DeptEvalGrade getDeptGrades(@PathVariable("deptId") String deptId) {
        return evaluationService.getDeptGrades(deptId);
    }

    @PostMapping("/save-grades")
    @ResponseBody
    public String saveGrades(@RequestBody DeptEvalGrade grade, @RequestParam(defaultValue = "false") boolean applyToChildren) {
        evaluationService.saveDeptGrades(grade, applyToChildren);
        return "success";
    }
}