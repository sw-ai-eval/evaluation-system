package com.eval.domain.multi.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eval.domain.multi.EvalSummaryChart;

public interface EvalSummaryRepository extends JpaRepository<EvalSummaryChart, Long> {

    Optional<EvalSummaryChart> findByEvaluateeNoAndEvalTypeId(String evaluateeNo,Integer evalTypeId);
    

	
}
