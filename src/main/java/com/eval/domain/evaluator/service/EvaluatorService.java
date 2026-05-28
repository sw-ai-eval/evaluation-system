package com.eval.domain.evaluator.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.employee.controller.EmployeeController;
import com.eval.domain.employee.service.EmployeeService;
import com.eval.domain.evaluation.EvalType;
import com.eval.domain.evaluation.repository.EvalTypeRepository;
import com.eval.domain.evaluator.EvalTargetMapping;
import com.eval.domain.evaluator.dto.AvailableEmployeeDto;
import com.eval.domain.evaluator.dto.EvaluatorDetailDto;
import com.eval.domain.evaluator.dto.EvaluatorDto;
import com.eval.domain.evaluator.dto.EvaluatorUpdateRequest;
import com.eval.domain.evaluator.dto.EvaluatorVeiwDto;
import com.eval.domain.evaluator.repository.EvaluatorRepository;
import com.eval.domain.finalresult.FinalResult;
import com.eval.domain.finalresult.FinalResultRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluatorService {

    	private final EmployeeController employeeController;
	
	 	private final EmployeeRepository employeeRepository;
	    private final EvaluatorRepository evaluatorRepository;
	    private final DepartmentRepository departmentRepository;
	    private final EvalTypeRepository evalTypeRepository;
	    private final FinalResultRepository finalResultRepository;
	    private final EmployeeService employeeService;
	    
	    @PersistenceContext
	    private EntityManager em;


	    // 평가자 리스트 
	    public List<EvaluatorVeiwDto> getEvaluatorList(String deptId, Integer typeId, String employeeSearch) {
	        List<EvaluatorVeiwDto> rows = evaluatorRepository.findFlatRows(deptId, typeId, employeeSearch);

	        Map<String, EvaluatorVeiwDto> map = new LinkedHashMap<>();

	        for (EvaluatorVeiwDto r : rows) {
	            EvaluatorVeiwDto dto = map.computeIfAbsent(r.getEmpNo(), k -> {
	                EvaluatorVeiwDto d = new EvaluatorVeiwDto();
	                d.setEmpNo(r.getEmpNo());
	                d.setEmpName(r.getEmpName());
	                d.setPosition(r.getPosition());
	                d.setDeptId(r.getDeptId());
	                d.setDeptName(r.getDeptName());
	                d.setStatus(0); // 초기값 0
	                d.setFirstEvaluators(new ArrayList<>());
	                d.setFirstEvaluatorsStatus(new ArrayList<>());
	                return d;
	            });

	            // 시스템 타입 세팅
	            if (r.getSystemType() != null) {
	                dto.setSystemType(r.getSystemType());
	            }

	            // 1차 평가자 합치기
	            r.getFirstEvaluators().forEach(eval -> {
	                if (dto.getFirstEvaluators().stream().noneMatch(e -> e.getEmpNo().equals(eval.getEmpNo()))) {
	                    dto.getFirstEvaluators().add(eval);
	                }
	            });

	            // 최종 평가자 세팅
	            if (r.getFinalEvaluatorEmpNo() != null && dto.getFinalEvaluatorEmpNo() == null) {
	                dto.setFinalEvaluatorEmpNo(r.getFinalEvaluatorEmpNo());
	                dto.setFinalEvaluatorName(r.getFinalEvaluatorName());
	            }

	            // status 리스트에 추가
	            dto.getFirstEvaluatorsStatus().add(r.getStatus());
	        }

	        // DTO별로 최종 status 계산
	     // DTO별로 최종 status 계산
	        map.values().forEach(dto -> {
	            List<Integer> statuses = dto.getFirstEvaluatorsStatus();

	            System.out.println("=== empNo: " + dto.getEmpNo() + " ===");
	            System.out.println("First Evaluators Status List: " + statuses);

	            int finalStatus;
	            if (statuses.isEmpty() || statuses.stream().allMatch(s -> s == 0)) {
	                finalStatus = 0; // 모두 0 → 버튼 활성
	            } else if (statuses.stream().allMatch(s -> s == 2)) {
	                finalStatus = 2; // 모두 2 → 완료
	            } else {
	                finalStatus = 1; 
	            }

	            dto.setStatus(finalStatus);
	            System.out.println("Final DTO Status: " + dto.getStatus());

	            // firstEvaluatorNames 합치기
	            dto.setFirstEvaluatorNames(
	                dto.getFirstEvaluators().stream()
	                    .map(EvaluatorDto::getName)
	                    .collect(Collectors.joining(", "))
	            );
	        });

	        return new ArrayList<>(map.values());
	    }
	    
	    
	    // 평가자 자도 ㅇ생성
	    @Transactional
	    public void createEvaluatorMapping(String deptId, int typeId) {

	        EvalType evalType = evalTypeRepository.findById(typeId)
	                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 typeId"));

	        List<Employee> employees = employeeRepository.findByDeptIdAndStatusNot(deptId,"RESIGNED");

	        if (employees == null || employees.isEmpty()) {
	            throw new IllegalArgumentException("해당 부서에 재직 중인 사원이 없습니다.");
	        }

	        // 부서장 Employee 객체 가져오기
	        String leaderEmpNo = employeeRepository.findDeptLeader(deptId);
	        if (leaderEmpNo == null) {
	            throw new IllegalStateException("해당 부서의 부서장이 존재하지 않습니다.");
	        }
	        
	        // findByEmpNo는 Employee를 바로 반환하므로, null 체크
	        Employee leader = employeeRepository.findByEmpNo(leaderEmpNo);
	        if (leader == null) {
	            throw new IllegalStateException("부서장 정보가 존재하지 않습니다.");
	        }
	        
	        String executive = employeeService.getExecutivesEmpNo(deptId);
	        
	        Employee executiveEmp = employeeRepository.findByEmpNo(executive);

	        if (executiveEmp == null) {
	            throw new IllegalStateException("임원 정보가 존재하지 않습니다.");
	        }

	        boolean isMultiEval = evalType.getName().contains("다면평가");

	        List<EvalTargetMapping> list = new ArrayList<>();


	        for (Employee e : employees) {

	            boolean isLeaderEmp = e.getEmpNo().equals(leader.getEmpNo());
	            boolean isExecutive = e.getEmpNo().equals(executiveEmp.getEmpNo());

	            // ================= 다면평가 =================
	            if (isMultiEval) {

	                if (!isLeaderEmp) continue;

	                boolean alreadyExists = evaluatorRepository.existsByEvaluateeNoAndDeptIdAndTypeId(
	                        leader.getEmpNo(), deptId, evalType);
	                if (alreadyExists) continue;

	                // 1차 : 부서원 -> 부서장
	                for (Employee member : employees) {
	                    boolean memberIsLeader = member.getEmpNo().equals(leader.getEmpNo());
	                    boolean memberIsExecutive = e.getEmpNo().equals(executiveEmp.getEmpNo());

	                    if (memberIsLeader || memberIsExecutive) continue;

	                    list.add(EvalTargetMapping.builder()
	                            .evaluatorNo(member.getEmpNo())
	                            .evaluateeNo(leader.getEmpNo())
	                            .step(1)
	                            .systemType("AUTO")
	                            .deptId(deptId)
	                            .typeId(evalType)
	                            .build());
	                }

	                // 2차 : 임원 -> 부서장
	                list.add(EvalTargetMapping.builder()
	                        .evaluatorNo(executiveEmp.getEmpNo())
	                        .evaluateeNo(leader.getEmpNo())
	                        .step(2)
	                        .systemType("AUTO")
	                        .deptId(deptId)
	                        .typeId(evalType)
	                        .build());


	                // FinalResult 생성
	                boolean finalExists = finalResultRepository.existsByEmployeeAndYear(leader, evalType.getYear());
	                if (!finalExists) {
	                    finalResultRepository.save(FinalResult.builder()
	                            .employee(leader)
	                            .year(evalType.getYear())
	                            .grade(null)
	                            .finalScore(null)
	                            .status(false)
	                            .weightRatio("{\"multi_eval\":1.0}")
	                            .createdAt(LocalDateTime.now())
	                            .updatedBy("SYSTEM")
	                            .build());
	                }

	                continue;
	            }

	            // ================= 일반 평가 =================
	            if (isLeaderEmp || isExecutive) continue;

	            boolean alreadyExists = evaluatorRepository.existsByEvaluateeNoAndDeptIdAndTypeId(
	                    e.getEmpNo(), deptId, evalType);
	            if (alreadyExists) continue;

	            // 0차 자기평가
	            list.add(EvalTargetMapping.builder()
	                    .evaluatorNo(e.getEmpNo())
	                    .evaluateeNo(e.getEmpNo())
	                    .step(0)
	                    .systemType("AUTO")
	                    .deptId(deptId)
	                    .typeId(evalType)
	                    .build());

	            // 1차 부서장 평가
	            list.add(EvalTargetMapping.builder()
	                    .evaluatorNo(leader.getEmpNo())
	                    .evaluateeNo(e.getEmpNo())
	                    .step(1)
	                    .systemType("AUTO")
	                    .deptId(deptId)
	                    .typeId(evalType)
	                    .build());

	            // 2차 임원 평가
	            list.add(EvalTargetMapping.builder()
	                    .evaluatorNo(executiveEmp.getEmpNo())
	                    .evaluateeNo(e.getEmpNo())
	                    .step(2)
	                    .systemType("AUTO")
	                    .deptId(deptId)
	                    .typeId(evalType)
	                    .build());


	            // FinalResult 생성
	            boolean finalExists = finalResultRepository.existsByEmployeeAndYear(e, evalType.getYear());
	            if (!finalExists) {
	                finalResultRepository.save(FinalResult.builder()
	                        .employee(e)
	                        .year(evalType.getYear())
	                        .grade(null)
	                        .finalScore(null)
	                        .status(false)
	                        .weightRatio("{\"performance\":0.6,\"competency\":0.4}")
	                        .createdAt(LocalDateTime.now())
	                        .updatedBy("SYSTEM")
	                        .build());
	            }
	        }

	        evaluatorRepository.saveAll(list);
	    }
	    
	    // 부서 초기화
	    @Transactional
	    public void resetEvaluatorMapping(String deptId, Integer typeId) {

	        EvalType targetType = evalTypeRepository.findById(typeId)
	                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 typeId"));

	        // 1. 삭제 대상 사원 리스트
	        List<String> evaluateeNos = evaluatorRepository.findEvaluateeNosByDeptIdAndTypeId(deptId, typeId);
	        if (evaluateeNos.isEmpty()) {
	            throw new IllegalStateException("초기화할 데이터가 없습니다.");
	        }

	        try {
	            // 2. evaluator 삭제
	            long deletedCount = evaluatorRepository.deleteByDeptIdAndTypeId_Id(deptId, typeId);
	            evaluatorRepository.flush(); // 즉시 DB 반영

	            // 3. final_result 삭제 로직
	            for (String empNo : evaluateeNos) {
	                Employee employee = employeeRepository.findByEmpNo(empNo);
	                if (employee == null) continue;

	                Optional<FinalResult> optionalFinal = finalResultRepository.findByEmployeeAndYear(employee, targetType.getYear());

	                if (optionalFinal.isPresent()) {
	                    // 같은 year에 다른 type이 남아 있는지 확인
	                    boolean otherTypeExists = evaluatorRepository.existsByEvaluateeNoAndTypeId_YearAndTypeId_IdNot(empNo, targetType.getYear(), typeId);
	                    if (!otherTypeExists) {
	                        // 남아있는 다른 평가가 없다면 final_result 삭제
	                        finalResultRepository.delete(optionalFinal.get());
	                    }
	                }
	            }

	        } catch (DataIntegrityViolationException e) {
	            throw new IllegalStateException("이미 일부 평가 답변(eval_answer)이 존재하여 전체 초기화할 수 없습니다. 답변 삭제 또는 개별 삭제를 먼저 수행해 주세요.");
	        }
	    }
	    
	    
	    // 세부사항 모달 
	    public EvaluatorDetailDto getDetail(String empNo, Integer typeId) {

	        List<EvalTargetMapping> list =
	                evaluatorRepository.findByEvaluateeNoAndTypeId_Id (empNo, typeId);

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
	        
	        EvalType evalType = m.getTypeId();  // EvalTargetMapping에서 타입 가져오기
	        dto.setEvalTypeName(evalType != null ? evalType.getName() : null);  // DTO에 세팅

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
	    
	    
	    
	    // 평가자 업데이트
	    @Transactional
	    public void update(EvaluatorUpdateRequest request, Integer typeId) {
	    	checkEvaluationStarted(request.getEmpNo(), typeId);
	    	
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        String adminNo = auth.getName();
	        
	        String empNo = request.getEmpNo();

	        updateFirstEvaluators(request, empNo, adminNo, typeId);
	        updateFinalEvaluator(request, empNo, adminNo, typeId);
	        
	        
	    }
	    
	    private void updateFirstEvaluators(EvaluatorUpdateRequest request, String empNo, String adminNo, Integer typeId) {
	    	List<EvalTargetMapping> dbList =evaluatorRepository.findByEvaluateeNoAndStepAndTypeId_Id(empNo, 1, typeId);
	    	
	    	EvalType type = em.getReference(EvalType.class, typeId);
	    	type.setId(typeId);
	    	
	    	Set<String> requestSet;
	        if (type.getName().contains("다면")) {
	            requestSet = new HashSet<>(request.getFirstEvaluators());
	        } else {
	            requestSet = request.getFirstEvaluators().isEmpty() ? Collections.emptySet() : Collections.singleton(request.getFirstEvaluators().get(0));
	        }
	    	
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
	    	        newData.setTypeId(type);
	    	        
	    	        evaluatorRepository.save(newData);
	    	    }
	    	}
	    	for (EvalTargetMapping db : dbList) {

	    	    if (!requestSet.contains(db.getEvaluatorNo())) {
	    	    	evaluatorRepository.delete(db);
	    	    }
	    	}

	    }
	    
	    
	    private void updateFinalEvaluator(EvaluatorUpdateRequest request, String empNo, String adminNo, Integer typeId) {

	        String newFinal = request.getFinalEvaluator();

	        Optional<EvalTargetMapping> existing =evaluatorRepository.findByEvaluateeNoAndStepAndTypeId_Id(empNo, 2, typeId).stream() .findFirst();

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
	            
	        	EvalType type = em.getReference(EvalType.class, typeId);
	        	type.setId(typeId);

	            newEntity.setEvaluateeNo(empNo);
	            newEntity.setEvaluatorNo(newFinal);
	            newEntity.setStep(2);
	            newEntity.setSystemType("MANUAL");
	            newEntity.setDeptId(request.getDeptId());
	            newEntity.setUpdatedBy(adminNo);
	            newEntity.setUpdatedAt(LocalDateTime.now());
	            newEntity.setTypeId(type);
	            
	            
	            evaluatorRepository.save(newEntity);
	        }
	    }
	    
	    // 피평가자 대상 제외
	    @Transactional
	    public void delete(String deptId, String empNo, Integer typeId) {
	        // 1. 평가 시작 여부 확인
	        checkEvaluationStarted(empNo, typeId);

	        // 2. 삭제 대상 사원 및 타입 정보 가져오기
	        EvalType targetType = evalTypeRepository.findById(typeId)
	                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 typeId"));

	        Employee employee = employeeRepository.findByEmpNo(empNo);
	        if (employee == null) {
	            throw new IllegalStateException("사원 정보가 존재하지 않습니다.");
	        }

	        // 3. evaluator 삭제
	        try {
	            evaluatorRepository.deleteByDeptIdAndEvaluateeNoAndTypeId_Id(deptId, empNo, typeId);
	        } catch (DataIntegrityViolationException e) {
	            throw new IllegalStateException("평가 답변(eval_answer)이 존재하여 삭제할 수 없습니다.");
	        }

	        // 4. final_result 확인
	        Optional<FinalResult> optionalFinal = finalResultRepository.findByEmployeeAndYear(employee, targetType.getYear());

	        if (optionalFinal.isPresent()) {
	            // 같은 year를 가진 다른 type이 있는지 확인
	            boolean otherTypeExists = evaluatorRepository.existsByEvaluateeNoAndTypeId_YearAndTypeId_IdNot(empNo, targetType.getYear(), typeId);

	            if (!otherTypeExists) {
	                // 같은 year에 다른 type이 없다면 final_result 삭제
	                finalResultRepository.delete(optionalFinal.get());
	            }
	        }
	    }
	    
	    

	    @Transactional
	    public void updateStatusToTwo(String evaluatorNo, String evaluateeNo, Integer typeIdInt) {
	        // Integer로 받은 evalTypeId를 EvalType 객체로 조회
	        EvalType evalType = evalTypeRepository.findById(typeIdInt)
	                .orElseThrow(() -> new RuntimeException("EvalType not found for id=" + typeIdInt));

	        // 매핑 조회
	        EvalTargetMapping mapping = evaluatorRepository.findByEvaluatorNoAndEvaluateeNoAndTypeId(
	                evaluatorNo, evaluateeNo, evalType
	        );

	        if (mapping != null) {
	            mapping.setStatus(2);
	            mapping.setUpdatedAt(LocalDateTime.now());
	            mapping.setUpdatedBy(evaluatorNo); // 필요 시 평가자
	            evaluatorRepository.save(mapping);
	            System.out.println("✅ Status updated for mappingId=" + mapping.getId());
	        } else {
	            System.out.println("⚠️ Mapping not found for evaluator=" + evaluatorNo 
	                               + ", evaluatee=" + evaluateeNo + ", evalType=" + typeIdInt);
	        }
	    }
	    
	    private void checkEvaluationStarted(String empNo, Integer typeId) {
	        boolean anyStarted = evaluatorRepository.existsByEvaluateeNoAndTypeId_IdAndStatusGreaterThan(empNo, typeId, 0);
	        if (anyStarted) {
	            throw new IllegalStateException("이미 평가가 진행된 항목이 있어 수정할 수 없습니다.");
	        }
	    }


		public void updateStatusToOne(String evaluatorNo, String evaluateeNo, Integer typeIdInt) {
			// TODO Auto-generated method stub
	        EvalType evalType = evalTypeRepository.findById(typeIdInt)
	                .orElseThrow(() -> new RuntimeException("EvalType not found for id=" + typeIdInt));

	        // 매핑 조회
	        EvalTargetMapping mapping = evaluatorRepository.findByEvaluatorNoAndEvaluateeNoAndTypeId(
	                evaluatorNo, evaluateeNo, evalType
	        );

	        if (mapping != null) {
	            mapping.setStatus(1);
	            mapping.setUpdatedAt(LocalDateTime.now());
	            mapping.setUpdatedBy(evaluatorNo); // 필요 시 평가자
	            evaluatorRepository.save(mapping);
	            System.out.println("✅ Status updated for mappingId=" + mapping.getId());
	        } else {
	            System.out.println("⚠️ Mapping not found for evaluator=" + evaluatorNo 
	                               + ", evaluatee=" + evaluateeNo + ", evalType=" + typeIdInt);
	        }
		}
}