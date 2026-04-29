package com.eval.domain.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eval.domain.evaluation.EvalItem;
import java.util.List;

public interface EvalItemRepository extends JpaRepository<EvalItem, Integer> {
    List<EvalItem> findByEvalTypeId(Integer typeId);
}