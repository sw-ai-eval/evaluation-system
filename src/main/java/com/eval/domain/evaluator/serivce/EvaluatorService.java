package com.eval.domain.evaluator.serivce;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.evaluator.EvalTargetMapping;
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
	    	
	    	if (deptId == null || deptId.isBlank()) {
	            return List.of();
	        }
	    	Department dept = departmentRepository.findById(deptId).orElse(null);

	    	String deptName = dept != null ? dept.getName() : "";
	    	
	        List<Employee> employees = employeeRepository.findByDeptId(deptId);
	        
	        if (employees == null || employees.isEmpty()) {
	            return List.of();
	        }
	        
	        List<String> empNos = employees.stream().map(Employee::getEmpNo).toList();

	        List<EvalTargetMapping> list = evaluatorRepository.findByEvaluateeNoIn(empNos);

	        Map<String, Employee> employeeMap = employees.stream().collect(Collectors.toMap(Employee::getEmpNo, e -> e));

	        Map<String, EvaluatorVeiwDto> map = new LinkedHashMap<>();

	        for (EvalTargetMapping m : list) {

	            String key = m.getEvaluateeNo();

	            Employee emp = employeeMap.get(key);
	            if (emp == null) continue;

	            EvaluatorVeiwDto dto = map.getOrDefault(key, new EvaluatorVeiwDto());

	            dto.setEmpName(emp.getName());
	            dto.setPosition(emp.getPosition());
	            dto.setDeptName(deptName);
	            dto.setUpdateBy(null);
	            dto.setUpdatedAt(null);

	            // -----------------------------------
	            // 1차 평가자
	            // -----------------------------------
	            if (m.getStep() == 1) {

	                String prev = dto.getFirstEvaluators();
	                String name = employeeMap.get(m.getEvaluatorNo()).getName();

	                dto.setFirstEvaluators(
	                        prev == null ? name : prev + ", " + name
	                );
	            }

	            // -----------------------------------
	            // 2차 평가자
	            // -----------------------------------
	            if (m.getStep() == 2) {

	                dto.setFinalEvaluator(
	                        employeeMap.get(m.getEvaluatorNo()).getName()
	                );
	            }

	            map.put(key, dto);
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
	    	
	        List<Employee> employees = employeeRepository.findByDeptIdAndStatus(deptId, "ACTIVE");
	        
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
	    
}