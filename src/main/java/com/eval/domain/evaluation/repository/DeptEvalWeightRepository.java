package com.eval.domain.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eval.domain.evaluation.DeptEvalWeight; // 엔티티 위치에 맞게 수정
import java.util.List;

public interface DeptEvalWeightRepository extends JpaRepository<DeptEvalWeight, Object> { // 복합키 ID 클래스 적용 필요
    List<DeptEvalWeight> findByDeptId(String deptId);
}