package com.eval.domain.multi;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EvalSummaryRepository extends JpaRepository<EvalSummaryChart, Long> {

    Optional<EvalSummaryChart> findByEvaluateeNoAndEvalTypeId(String evaluateeNo,Integer evalTypeId);
    
    @Query("""
    		SELECT e.totalScore
    		FROM EvalSummaryChart e
    		WHERE e.evalTypeId = :evalTypeId
    		AND e.evaluateeNo = :leaderNo
    		""")
    		BigDecimal findMyTotalScore(Long evalTypeId, String leaderNo);
    @Query("""
    		SELECT AVG(e.totalScore)
    		FROM EvalSummaryChart e
    		WHERE e.evalTypeId = :evalTypeId
    		""")
    		BigDecimal findAvgTotalScore(Long evalTypeId);
    @Query("""
    		SELECT e.avgScore
    		FROM EvalSummaryChart e
    		WHERE e.evalTypeId = :evalTypeId
    		AND e.evaluateeNo = :leaderNo
    		""")
    		BigDecimal findMyAvgScore(Long evalTypeId, String leaderNo);

    @Query("""
    		SELECT AVG(e.avgScore)
    		FROM EvalSummaryChart e
    		WHERE e.evalTypeId = :evalTypeId
    		""")
    		BigDecimal findAllLeaderAvgScore(Long evalTypeId);
}
