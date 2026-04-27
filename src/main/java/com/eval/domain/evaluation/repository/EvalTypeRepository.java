package com.eval.domain.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eval.domain.evaluation.EvalType; // 엔티티 위치에 맞게 수정
import java.util.List;

public interface EvalTypeRepository extends JpaRepository<EvalType, Integer> {
    List<EvalType> findByYear(Integer year);
}