package com.eval.domain.multi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eval.domain.multi.EvalCategorySummary;

public interface EvalCategorySummaryRepository extends JpaRepository<EvalCategorySummary, Long> {

	List<EvalCategorySummary> findByEvaluateeNoAndEvalTypeId(String evaluateeNo, long longValue);
	
    Optional<EvalCategorySummary> findByEvaluateeNoAndEvalTypeIdAndCategoryName(String evaluateeNo, Long evalTypeId, String categoryName);


}