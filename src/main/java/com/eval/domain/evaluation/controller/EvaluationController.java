package com.eval.domain.evaluation.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eval.domain.evaluation.EvalType;
import com.eval.domain.evaluation.EvalItem;
import com.eval.domain.dept.Department;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.evaluation.DeptEvalWeight;
import com.eval.domain.evaluation.repository.EvalTypeRepository;
import com.eval.domain.evaluation.repository.EvalItemRepository;
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

    public EvaluationController(DepartmentRepository departmentRepository, 
                                EvaluationService evaluationService,
                                EvalTypeRepository evalTypeRepository,
                                EvalItemRepository evalItemRepository) {
        this.departmentRepository = departmentRepository;
        this.evaluationService = evaluationService;
        this.evalTypeRepository = evalTypeRepository;
        this.evalItemRepository = evalItemRepository;
    }

    @GetMapping
    public String evaluationSetting(Model model) {
        // 1. 부서 목록 조회
        List<Department> deptList = departmentRepository.findAll();
        model.addAttribute("deptList", deptList);
        
        // 2. 평가 유형 목록 조회
        List<EvalType> typeList = evalTypeRepository.findAll();
        model.addAttribute("typeList", typeList);
        
        // 3. 평가 문항 목록 조회 (전체)
        List<EvalItem> itemList = evalItemRepository.findAll();
        model.addAttribute("itemList", itemList);
        
        return "evaluation/setting";
    }

    // --- 평가 유형 저장 ---
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
            log.error("❌ 유형 저장 오류: ", e);
            return "error: " + e.getMessage();
        }
    }

    // --- 평가 문항 저장 (DDL 명세에 맞춰 완벽 수정) ---
    @PostMapping("/save-item")
    @ResponseBody
    public String saveEvalItem(@RequestBody Map<String, Object> params) {
        try {
            EvalItem item = new EvalItem();
            
            // 1. 수정 시 ID 처리
            if (params.get("id") != null && !params.get("id").toString().isEmpty()) {
                item = evalItemRepository.findById(Integer.parseInt(params.get("id").toString())).orElseThrow();
            }

            // 2. 부모인 EvalType 연결
            Integer typeId = Integer.parseInt(params.get("typeId").toString());
            EvalType type = evalTypeRepository.findById(typeId).orElseThrow();
            
            item.setEvalType(type);
            item.setCategory(params.get("category").toString());
            
            // 🌟 DDL 컬럼명 매핑 (content -> question)
            item.setContent(params.get("content").toString()); 
            
            // 🌟 DDL 컬럼명 매핑 (answerType -> question_type)
            item.setAnswerType(params.get("answerType").toString()); 
            
            // 🌟 DDL is_common은 BIT 타입이므로 boolean으로 처리 ("Y"일 때 true)
            item.setCommon("Y".equals(params.get("isCommon").toString()));
            
            // 🌟 DDL explanation (NOT NULL 제약조건 대응)
            String category = params.get("category").toString();
            item.setExplanation(category + " 항목에 대한 세부 평가 문항입니다.");

            if (item.getId() == null) {
                // 신규 등록
                item.setCreatedBy("ADMIN");
                // createdAt은 엔티티 초기값 사용
            } else {
                // 수정
                item.setUpdatedBy("ADMIN");
                item.setUpdatedAt(LocalDateTime.now());
            }

            // 🌟 DDL에 없는 sortOrder 세팅 로직은 완전히 제거함

            evalItemRepository.save(item);
            return "success";
        } catch (Exception e) {
            log.error("❌ 문항 저장 오류: ", e);
            return "error: " + e.getMessage();
        }
    }

    // --- 평가 문항 삭제 ---
    @PostMapping("/delete-item/{id}")
    @ResponseBody
    public String deleteEvalItem(@PathVariable("id") Integer id) {
        try {
            evalItemRepository.deleteById(id);
            return "success";
        } catch (Exception e) {
            log.error("❌ 문항 삭제 오류: ", e);
            return "error: " + e.getMessage();
        }
    }

    // --- 평가 유형 삭제 ---
    @PostMapping("/delete-type/{id}")
    @ResponseBody
    public String deleteEvalType(@PathVariable("id") Integer id) {
        try {
            evalTypeRepository.deleteById(id);
            return "success";
        } catch (DataIntegrityViolationException e) {
            return "error: 사용 중인 유형은 삭제할 수 없습니다.";
        } catch (Exception e) {
            return "error: 서버 오류";
        }
    }
    
    // --- 부서 가중치 저장 ---
    @PostMapping("/save-weights")
    @ResponseBody
    public String saveWeights(@RequestBody List<DeptEvalWeight> weights) {
        try {
            if (weights == null || weights.isEmpty()) {
                return "error: 데이터 없음";
            }
            evaluationService.saveDeptWeights(weights.get(0).getDeptId(), weights);
            return "success";
        } catch (Exception e) {
            log.error("❌ 가중치 저장 오류: ", e);
            return "error: " + e.getMessage();
        }
    }
}