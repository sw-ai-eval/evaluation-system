package com.eval.domain.evaluator.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.evaluator.EvalTargetMapping;
import com.eval.domain.evaluator.dto.AvailableEmployeeDto;
import com.eval.domain.evaluator.dto.EvaluatorDetailDto;
import com.eval.domain.evaluator.dto.EvaluatorDto;
import com.eval.domain.evaluator.dto.EvaluatorUpdateRequest;
import com.eval.domain.evaluator.dto.EvaluatorVeiwDto;
import com.eval.domain.evaluator.repository.EvaluatorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluatorService {
	
	 	private final EmployeeRepository employeeRepository;
	    private final EvaluatorRepository evaluatorRepository;
	    private final DepartmentRepository departmentRepository;
	    
	    // 평가자 리스트 
	    public List<EvaluatorVeiwDto> getEvaluatorList(String deptId) {

	        List<EvaluatorVeiwDto> rows = evaluatorRepository.findFlatRows(deptId);

	        Map<String, EvaluatorVeiwDto> map = new LinkedHashMap<>();

	        for (EvaluatorVeiwDto r : rows) {

	            EvaluatorVeiwDto dto = map.computeIfAbsent(r.getEmpNo(), k -> {
	                EvaluatorVeiwDto d = new EvaluatorVeiwDto();
	                d.setEmpNo(r.getEmpNo());
	                d.setEmpName(r.getEmpName());
	                d.setPosition(r.getPosition());
	                d.setDeptId(r.getDeptId());
	                d.setDeptName(r.getDeptName());
	                d.setFirstEvaluators(new ArrayList<>());
	                return d;
	            });
	            
	            if ("AUTO".equals(dto.getSystemType())|| (r.getSystemType() != null &&"MANUAL".equals(r.getSystemType()))) {
	                    dto.setSystemType(r.getSystemType());
	            }

	            // 1차 평가자
	            if (r.getFirstEvaluators() != null) {

	                for (EvaluatorDto eval : r.getFirstEvaluators()) {

	                    boolean exists = dto.getFirstEvaluators()
	                                        .stream()
	                                        .anyMatch(e -> e.getEmpNo()
	                                                        .equals(eval.getEmpNo()));

	                    if (!exists) {
	                        dto.getFirstEvaluators().add(eval);
	                    }
	                }
	            }

	            // 2차 평가자
	            if (r.getFinalEvaluatorEmpNo() != null && dto.getFinalEvaluatorEmpNo() == null) {

	                dto.setFinalEvaluatorEmpNo(r.getFinalEvaluatorEmpNo());
	                dto.setFinalEvaluatorName(r.getFinalEvaluatorName());
	            }
	        }
	        for (EvaluatorVeiwDto dto : map.values()) {

	            dto.setFirstEvaluatorNames(
	                dto.getFirstEvaluators().stream()
	                    .map(EvaluatorDto::getName)
	                    .collect(Collectors.joining(", "))
	            );
	        }
	        return new ArrayList<>(map.values());
	    }
	    
	    
	    // 평가자 자도 ㅇ생성
	    @Transactional
	    public void createEvaluatorMapping(String deptId) {
	    	
	    	boolean exists = evaluatorRepository.existsByDeptId(deptId);
	        if (exists) {
	            throw new IllegalStateException("이미 해당 부서의 평가 매핑이 존재합니다.");
	        }
	    	
	        List<Employee> employees = employeeRepository.findByDeptId(deptId);
	        
	        if (employees == null || employees.isEmpty()) {
	            throw new IllegalArgumentException("해당 부서에 재직 중인 사원이 없습니다.");
	        }
	        
	        String leader = employeeRepository.findDeptLeader(deptId);
	        List<Employee> executives = employeeRepository.findByLevelId(6);
	        
	        int execIndex = 0;
	        int execSize = executives.size();
	        
	        if (leader == null) {
	            throw new IllegalStateException("해당 부서의 부서장이 존재하지 않습니다.");
	        }

	        if (execSize == 0) {
	            throw new IllegalStateException("임원 정보가 존재하지 않습니다.");
	        }
	        
	        List<EvalTargetMapping> list = new ArrayList<>();

	        for (Employee e : employees) {

	            boolean isLeader = e.getEmpNo().equals(leader);
	            boolean isExecutive = executives.stream().anyMatch(ex -> ex.getEmpNo().equals(e.getEmpNo()));

	            if (!isExecutive) {

	                // 0차 자기평가 (부서장, 부서원)
	                list.add(EvalTargetMapping.builder()
	                        .evaluatorNo(e.getEmpNo())
	                        .evaluateeNo(e.getEmpNo())
	                        .step(0)
	                        .systemType("AUTO")
	                        .deptId(deptId)
	                        .build());
	            }

	            // 1차 평가
	            if (!isExecutive && !isLeader) {

	                // 부서원 → 부서장
	                list.add(EvalTargetMapping.builder()
	                        .evaluatorNo(e.getEmpNo())
	                        .evaluateeNo(leader)
	                        .step(1)
	                        .systemType("AUTO")
	                        .deptId(deptId)
	                        .build());

	                // 부서장 → 부서원
	                list.add(EvalTargetMapping.builder()
	                        .evaluatorNo(leader)
	                        .evaluateeNo(e.getEmpNo())
	                        .step(1)
	                        .systemType("AUTO")
	                        .deptId(deptId)
	                        .build());
	            }

	          
	            // 2차 평가 임원 -> 사원
	            if (!isExecutive) {

	                Employee exec = executives.get(execIndex % execSize);

	                list.add(EvalTargetMapping.builder()
	                        .evaluatorNo(exec.getEmpNo())
	                        .evaluateeNo(e.getEmpNo())
	                        .step(2)
	                        .systemType("AUTO")
	                        .deptId(deptId)
	                        .build());

	                execIndex++;
	            }
	        }

	        evaluatorRepository.saveAll(list);
	    }
	    
	    @Transactional
	    public void resetEvaluatorMapping(String deptId) {

	        long deletedCount = evaluatorRepository.deleteByDeptId(deptId);

	        if (deletedCount == 0) {
	            throw new IllegalStateException("초기화할 데이터가 없습니다.");
	        }
	    }
	    
	    
	    
	    public EvaluatorDetailDto getDetail(String empNo) {

	        List<EvalTargetMapping> list =
	                evaluatorRepository.findByEvaluateeNo(empNo);

	        if (list.isEmpty()) {
	            throw new IllegalArgumentException("데이터 없음");
	        }

	        EvalTargetMapping m = list.get(0);

	        Employee evaluatee =employeeRepository.findByEmpNo(empNo);

	        Department dept = departmentRepository.findById(m.getDeptId()).orElse(null);

	        EvaluatorDetailDto dto = new EvaluatorDetailDto();

	        dto.setDeptId(m.getDeptId());
	        dto.setDeptName(dept != null ? dept.getName() : null);

	        dto.setEmpNo(empNo);
	        dto.setEmpName(evaluatee != null ? evaluatee.getName() : null);
	        dto.setPosition(evaluatee != null ? evaluatee.getPosition() : null);

	        // 평가자
	        Set<String> empNos = list.stream().map(EvalTargetMapping::getEvaluatorNo).collect(Collectors.toSet());

	        List<Employee> employees =employeeRepository.findByEmpNoIn(empNos);

	        Map<String, Employee> empMap = employees.stream().collect(Collectors.toMap(Employee::getEmpNo, e -> e));

	        List<EvaluatorDto> firstList = new ArrayList<>();
	        String finalEvalEmpNo = null;
	        String finalEvalEmpName=null;

	        for (EvalTargetMapping item : list) {

	            Employee e = empMap.get(item.getEvaluatorNo());
	            if (e == null) continue;

	            if (item.getStep() == 1) {
	            	firstList.add(
	            		    new EvaluatorDto(
	            		        e.getEmpNo(),
	            		        e.getName()
	            		    )
	            		);
	            }

	            if (item.getStep() == 2 && finalEvalEmpNo == null) {
	                finalEvalEmpNo = e.getEmpNo();  // ⭐ 핵심
	                finalEvalEmpName=e.getName();
	            }
	        }
	        List<Employee> deptEmployees =employeeRepository.findByDeptId(m.getDeptId());
	        
	        List<AvailableEmployeeDto> allEmployees =
	        	    deptEmployees.stream()
	        	        .filter(e -> !e.getEmpNo().equals(empNo))
	        	        .map(e -> new AvailableEmployeeDto(e.getEmpNo(), e.getName()))
	        	        .collect(Collectors.toList());

	        dto.setAvailableEmployees(allEmployees);

	        dto.setFirstEvaluators(firstList);
	        dto.setFinalEvaluator(finalEvalEmpNo);
	        dto.setFinalEvaluatorName(finalEvalEmpName);

	        return dto;
	    }
	    
	    
	    
	    
	    @Transactional
	    public void update(EvaluatorUpdateRequest request) {
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        String adminNo = auth.getName();
	        
	        String empNo = request.getEmpNo();

	        updateFirstEvaluators(request, empNo, adminNo);
	        updateFinalEvaluator(request, empNo, adminNo);
	        
	        
	    }
	    
	    private void updateFirstEvaluators(EvaluatorUpdateRequest request, String empNo, String adminNo) {
	    	List<EvalTargetMapping> dbList =evaluatorRepository.findByEvaluateeNoAndStep(empNo, 1);
	    	
	    	Set<String> requestSet = new HashSet<>(request.getFirstEvaluators());
	    	
	    	
	    	for (String evaluator : requestSet) {

	    	    boolean exists = dbList.stream()
	    	            .anyMatch(e -> e.getEvaluatorNo().equals(evaluator));

	    	    if (!exists) {
	    	        EvalTargetMapping newData = new EvalTargetMapping();
	    	        newData.setEvaluateeNo(empNo);
	    	        newData.setEvaluatorNo(evaluator);
	    	        newData.setStep(1);
	    	        newData.setSystemType("MANUAL");
	    	        newData.setDeptId(request.getDeptId());
	    	        newData.setUpdatedBy(adminNo);
	    	        newData.setUpdatedAt(LocalDateTime.now());

	    	        evaluatorRepository.save(newData);
	    	    }
	    	}
	    	for (EvalTargetMapping db : dbList) {

	    	    if (!requestSet.contains(db.getEvaluatorNo())) {
	    	    	evaluatorRepository.delete(db);
	    	    }
	    	}

	    }
	    
	    
	    private void updateFinalEvaluator(EvaluatorUpdateRequest request, String empNo, String adminNo) {

	        String newFinal = request.getFinalEvaluator();

	        Optional<EvalTargetMapping> existing =evaluatorRepository.findByEvaluateeNoAndStep(empNo, 2).stream() .findFirst();

	        // ⭐ 최종 평가자 제거
	        if (newFinal == null || newFinal.isBlank()) {

	            existing.ifPresent(evaluatorRepository::delete);
	            return;
	        }

	        // ⭐ 기존 데이터 있음
	        if (existing.isPresent()) {

	            EvalTargetMapping entity = existing.get();

	            boolean isSame =Objects.equals(entity.getEvaluatorNo(), newFinal);

	            // ⭐ 변경된 경우만 update
	            if (!isSame) {

	                entity.setEvaluatorNo(newFinal);
	                entity.setSystemType("MANUAL");
	                entity.setUpdatedBy(adminNo);
	                entity.setUpdatedAt(LocalDateTime.now());
	            }

	        } else {

	            // ⭐ 신규 생성
	            EvalTargetMapping newEntity = new EvalTargetMapping();

	            newEntity.setEvaluateeNo(empNo);
	            newEntity.setEvaluatorNo(newFinal);
	            newEntity.setStep(2);
	            newEntity.setSystemType("MANUAL");
	            newEntity.setDeptId(request.getDeptId());
	            newEntity.setUpdatedBy(adminNo);
	            newEntity.setUpdatedAt(LocalDateTime.now());

	            evaluatorRepository.save(newEntity);
	        }
	    }
	    
	    
	    
}