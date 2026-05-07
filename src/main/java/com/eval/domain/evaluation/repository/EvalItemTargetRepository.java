package com.eval.domain.evaluation.repository;

import com.eval.domain.evaluation.EvalItemTarget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvalItemTargetRepository extends JpaRepository<EvalItemTarget, Integer> {
    // 특정 문항에 매핑된 타겟들을 한 번에 지우기 위한 메서드
    void deleteByQuestionId(Integer questionId);
}