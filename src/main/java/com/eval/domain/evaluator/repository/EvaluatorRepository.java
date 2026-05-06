package com.eval.domain.evaluator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eval.domain.evaluator.EvalTargetMapping;

public interface EvaluatorRepository extends JpaRepository<EvalTargetMapping, Long> {
	
	List<EvalTargetMapping> findByEvaluateeNoIn(List<String> empNos);	
	
	boolean existsByDeptId(String deptId);
	
}