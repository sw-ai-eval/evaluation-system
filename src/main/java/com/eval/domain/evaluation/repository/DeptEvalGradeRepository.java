package com.eval.domain.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eval.domain.evaluation.DeptEvalGrade;
import org.springframework.stereotype.Repository;

@Repository
public interface DeptEvalGradeRepository extends JpaRepository<DeptEvalGrade, String> {
}