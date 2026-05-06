package com.eval.domain.evaluation.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
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
    public String evaluationSetting(
            @RequestParam(value = "typePage", defaultValue = "0") int typePageNum,
            @RequestParam(value = "itemPage", defaultValue = "0") int itemPageNum,
            Model model) {
        
        // 1. 부서 목록 (가중치 설정용)
        List<Department> deptList = departmentRepository.findAll();
        model.addAttribute("deptList", deptList);
        
        // 2. 모달창 내 '대상 유형' 셀렉트 박스용 전체 목록
        List<EvalType> typeList = evalTypeRepository.findAll();
        model.addAttribute("typeList", typeList);
        
        // 3. 가중치 탭용 동적 목록 (운영 상태 '활성' + 가중치 반영 'Y'만 가져옴)
        List<EvalType> weightTypes = evalTypeRepository.findByStatusTrueAndHasWeightTrue();
        model.addAttribute("weightTypes", weightTypes);
        
        int pageSize = 10;
        
        // 4. 평가 유형 페이징 목록 (표 출력용, 등록순 ASC)
        PageRequest typePageReq = PageRequest.of(typePageNum, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<EvalType> typePage = evalTypeRepository.findAll(typePageReq);
        model.addAttribute("typePage", typePage);
        
        // 5. 평가 문항 페이징 목록 (표 출력용, 등록순 ASC)
        PageRequest itemPageReq = PageRequest.of(itemPageNum, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<EvalItem> itemPage = evalItemRepository.findAll(itemPageReq);
        model.addAttribute("itemPage", itemPage);
        
        return "evaluation/setting";
    }

    // --- 평가 유형 저장 ---
    @PostMapping("/save-type")
    @ResponseBody
    public String saveEvalType(@RequestBody EvalType evalType) {
        try {
            boolean isDuplicate = false;
            
            if (evalType.getId() == null) {
                // 신규 등록 시 연도+이름 중복 체크
                isDuplicate = evalTypeRepository.existsByYearAndName(evalType.getYear(), evalType.getName());
            } else {
                // 수정 시 기존 연도나 이름과 달라졌을 때만 중복 체크
                EvalType existing = evalTypeRepository.findById(evalType.getId()).orElseThrow();
                if (!existing.getName().equals(evalType.getName()) || !existing.getYear().equals(evalType.getYear())) {
                    isDuplicate = evalTypeRepository.existsByYearAndName(evalType.getYear(), evalType.getName());
                }
            }

            if (isDuplicate) {
                return "error: 이미 해당 연도에 [" + evalType.getName() + "] 평가 유형이 존재합니다.";
            }

            if (evalType.getId() != null) {
                EvalType existing = evalTypeRepository.findById(evalType.getId()).orElseThrow();
                existing.setName(evalType.getName());
                existing.setYear(evalType.getYear());
                existing.setStartDate(evalType.getStartDate());
                existing.setEndDate(evalType.getEndDate());
                existing.setStatus(evalType.isStatus());
                existing.setGuideline(evalType.getGuideline());
                existing.setHasWeight(evalType.isHasWeight()); // 가중치 반영 여부 업데이트
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

    // --- 평가 문항 저장 ---
    @PostMapping("/save-item")
    @ResponseBody
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
            
            if (params.get("weight") != null && !params.get("weight").toString().isEmpty()) {
                item.setWeight(Integer.parseInt(params.get("weight").toString()));
            } else {
                item.setWeight(0);
            }
            
            String category = params.get("category").toString();
            item.setExplanation(category + " 항목에 대한 세부 평가 문항입니다.");

            if (item.getId() == null) {
                item.setCreatedBy("ADMIN");
            } else {
                item.setUpdatedBy("ADMIN");
                item.setUpdatedAt(LocalDateTime.now());
            }

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
            int totalWeight = weights.stream().mapToInt(DeptEvalWeight::getWeight).sum();
            if (totalWeight != 100) {
                return "error: 가중치 합계가 100%가 아닙니다. (현재 서버에서 계산된 합계: " + totalWeight + "%)";
            }
            evaluationService.saveDeptWeights(weights.get(0).getDeptId(), weights);
            return "success";
        } catch (Exception e) {
            log.error("❌ 가중치 저장 오류: ", e);
            return "error: " + e.getMessage();
        }
    }
}