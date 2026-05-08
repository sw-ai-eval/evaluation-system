package com.eval.domain.evaluator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eval.domain.evaluator.EvalTargetMapping;
import com.eval.domain.evaluator.dto.EvaluatorVeiwDto;

public interface EvaluatorRepository extends JpaRepository<EvalTargetMapping, Long> {
	
	List<EvalTargetMapping> findByEvaluateeNoIn(List<String> empNos);	
	
	boolean existsByDeptId(String deptId);
	
	Long deleteByDeptId(String deptId);
	
	
    // 피평가자 기준 전체 조회
    List<EvalTargetMapping> findByEvaluateeNo(String evaluateeNo);
    
    
    @Query("""
    		SELECT new com.eval.domain.evaluator.dto.EvaluatorVeiwDto(
    		    m.deptId,
    		    d.name,
    		    e2.empNo,
    		    e2.name,
    		    e2.position,
    		    m.step,
    		    e1.empNo,
    		    e1.name,
    		    m.systemType
    		)
    		FROM EvalTargetMapping m
    		JOIN Employee e1 ON m.evaluatorNo = e1.empNo
    		JOIN Employee e2 ON m.evaluateeNo = e2.empNo
    		JOIN Department d ON m.deptId = d.id
    		WHERE m.deptId = :deptId
    		""")
    		List<EvaluatorVeiwDto> findFlatRows(@Param("deptId") String deptId);
    
    // 피평가자 기준 평가 단계로 데이터 조회
    List<EvalTargetMapping> findByEvaluateeNoAndStep(String evaluateeNo, int step);
    
    // 피평가자 기준 평가자, 단계 조회
    Optional<EvalTargetMapping> findByEvaluateeNoAndEvaluatorNoAndStep(String evaluateeNo, String evaluatorNo, int step);
    
    
    void delete(EvalTargetMapping entity);

}