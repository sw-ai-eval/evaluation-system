package com.eval.domain.evaluation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.evaluation.DeptEvalGrade;
import com.eval.domain.evaluation.DeptEvalWeight;
import com.eval.domain.evaluation.EvalItemTarget;
import com.eval.domain.evaluation.EvalType;
import com.eval.domain.evaluation.repository.DeptEvalGradeRepository;
import com.eval.domain.evaluation.repository.DeptEvalWeightRepository;
import com.eval.domain.evaluation.repository.EvalItemTargetRepository;
import com.eval.domain.evaluation.repository.EvalTypeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    
    private final DeptEvalWeightRepository weightRepository;
    private final DeptEvalGradeRepository gradeRepository;
    private final DepartmentRepository departmentRepository;
    private final EvalItemTargetRepository targetRepository;
    private final EmployeeRepository employeeRepository; // 🌟 추가됨
    private final EvalTypeRepository evalTypeRepository; // 김규리가 추가함 

    // --- [가중치 영역] ---

    // 특정 부서의 가중치 목록 조회
    public List<DeptEvalWeight> getDeptWeights(String deptId) {
        return weightRepository.findByDeptId(deptId);
    }

    // 부서 가중치 저장
    @Transactional
    public void saveDeptWeights(String targetDeptId, List<DeptEvalWeight> weights, boolean applyToChildren) {
        int total = weights.stream().mapToInt(DeptEvalWeight::getWeight).sum();
        if (total != 100) {
            throw new IllegalArgumentException("가중치 합계는 반드시 100% 여야 합니다.");
        }

        // 1. 선택한 부서 저장
        saveSingleDeptWeights(targetDeptId, weights);

        // 2. 하위 조직 일괄 적용 체크 시
        if (applyToChildren) {
            List<Department> allDepts = departmentRepository.findAll();
            List<String> childDeptIds = getDescendantDeptIds(allDepts, targetDeptId);
            
            for (String childId : childDeptIds) {
                saveSingleDeptWeights(childId, weights);
            }
        }
    }

    // 가중치 단건 저장 로직 (중복 방지를 위한 내부 메서드)
    private void saveSingleDeptWeights(String deptId, List<DeptEvalWeight> weights) {
        weightRepository.deleteByDeptId(deptId); 
        
        for (DeptEvalWeight w : weights) {
            DeptEvalWeight newWeight = new DeptEvalWeight();
            newWeight.setDeptId(deptId);
            newWeight.setTypeId(w.getTypeId());
            newWeight.setWeight(w.getWeight());
            weightRepository.save(newWeight);
        }
    }

    // 하위 부서 ID 추적
    private List<String> getDescendantDeptIds(List<Department> allDepts, String parentId) {
        List<String> descendants = new ArrayList<>();
        for (Department dept : allDepts) {
            // Department 엔티티의 부모ID 필드명(parentId) 확인
            if (dept.getParentId() != null && dept.getParentId().equals(parentId)) {
                descendants.add(dept.getId());
                descendants.addAll(getDescendantDeptIds(allDepts, dept.getId()));
            }
        }
        return descendants;
    }

 // --- [등급 영역] ---

    // 조직별 등급 기준 조회
    public DeptEvalGrade getDeptGrades(String deptId) {
        return gradeRepository.findById(deptId).orElse(null);
    }

    // 조직별 등급 기준 저장 (일괄 적용 로직 추가)
    @Transactional
    public void saveDeptGrades(DeptEvalGrade grade, boolean applyToChildren) {
        // 1. 선택한 부서 본인 저장
        gradeRepository.save(grade);

        // 2. 하위 조직 일괄 적용 체크 시
        if (applyToChildren) {
            List<Department> allDepts = departmentRepository.findAll();
            List<String> childDeptIds = getDescendantDeptIds(allDepts, grade.getDeptId());
            
            for (String childId : childDeptIds) {
                // 부모와 동일한 등급 수치로 객체 생성
                DeptEvalGrade childGrade = new DeptEvalGrade(
                    childId, 
                    grade.getGradeS(), 
                    grade.getGradeA(), 
                    grade.getGradeB(), 
                    grade.getGradeC(), 
                    grade.getGradeD()
                );
                gradeRepository.save(childGrade);
            }
        }
    }
    
    @Transactional
    public void saveItemTargets(Integer questionId, List<Map<String, String>> targets) {
        // 1. 기존 삭제
        targetRepository.deleteByQuestionId(questionId);

        // 2. 신규 저장
        if (targets != null && !targets.isEmpty()) {
            for (Map<String, String> targetMap : targets) {
                EvalItemTarget target = new EvalItemTarget();
                target.setQuestionId(questionId);
                target.setTargetType(targetMap.get("targetType")); // 'DEPT' or 'EMP'
                target.setTargetValue(targetMap.get("targetValue")); // deptId or empNo
                targetRepository.save(target);
            }
        }
    }
    
    public List<Map<String, Object>> getItemTargets(Integer questionId) {
        List<EvalItemTarget> targets = targetRepository.findByQuestionId(questionId);
        
        return targets.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("targetType", t.getTargetType());
            map.put("targetValue", t.getTargetValue());
            
            // 기본값은 사번이나 부서 ID로 세팅
            String displayName = t.getTargetValue(); 
            
            if ("DEPT".equals(t.getTargetType())) {
                // 부서인 경우: DepartmentRepository에서 부서명을 찾아옴
                departmentRepository.findById(t.getTargetValue())
                    .ifPresent(dept -> map.put("targetName", dept.getName()));
            } else if ("EMP".equals(t.getTargetType())) {
                // 사원인 경우: EmployeeRepository에서 사원명을 찾아옴
                employeeRepository.findById(t.getTargetValue())
                    .ifPresent(emp -> map.put("targetName", emp.getName())); // 엔티티의 이름을 꺼내옴
            }
            
            // 만약 퇴사했거나 삭제된 부서라서 이름을 못 찾았다면 그냥 원래 ID를 보여줌
            map.putIfAbsent("targetName", displayName);

            return map;
        }).collect(Collectors.toList());
    }
    
    // 김규리가 추가함
    public List<EvalType> findAll() {
        return evalTypeRepository.findAll();
    }
}