package com.eval.domain.multi;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MultiEvalAnswerRepository extends JpaRepository<MultiEvalAnswer, Long> {

	Optional<MultiEvalAnswer> findByQuestionIdAndMappingId(Long questionId, Long mappingId);

	Optional<MultiEvalAnswer> findByMappingIdAndQuestionId(Long mappingId, Long questionId);


    List<MultiEvalAnswer> findByMappingIdIn(List<Long> mappingIds);
	
	List<MultiEvalAnswer> findByMappingId(Long mappingId);
	
	@Query("""
		    SELECT COALESCE(SUM(a.score), 0)
		    FROM MultiEvalAnswer a
		    JOIN EvalTargetMapping m ON a.mappingId = m.id
		    WHERE m.typeId.id = :evalTypeId
		      AND m.evaluateeNo = :evaluateeNo
		""")
		BigDecimal getTotalScore(
		    @Param("evaluateeNo") String evaluateeNo,
		    @Param("evalTypeId") Integer evalTypeId
		);


		@Query("""
		    SELECT COALESCE(AVG(t.total), 0)
		    FROM (
		        SELECT SUM(a.score) AS total
		        FROM MultiEvalAnswer a
		        JOIN EvalTargetMapping m ON a.mappingId = m.id
		        WHERE m.typeId.id = :evalTypeId
		        GROUP BY m.evaluateeNo
		    ) t
		""")
		BigDecimal getAvgPerEvaluatee(
		    @Param("evalTypeId") Integer evalTypeId
		);

		@Query("""
				SELECT AVG(a.score)
				FROM MultiEvalAnswer a
				WHERE a.mapping.evaluateeNo = :evaluateeNo
				AND a.mapping.typeId = :evalTypeId
				""")
				BigDecimal getMyAvgScore(String evaluateeNo, Integer evalTypeId);
}
