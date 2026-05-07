package com.eval.domain.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eval.domain.evaluation.DeptEvalWeight;
import java.util.List;

public interface DeptEvalWeightRepository extends JpaRepository<DeptEvalWeight, DeptEvalWeight.DeptEvalWeightId> { 
    
    // 특정 부서의 가중치 목록 찾기 (조회용)
    List<DeptEvalWeight> findByDeptId(String deptId);
    
    // 특정 부서의 가중치 일괄 삭제 (저장 시 덮어쓰기용)
    void deleteByDeptId(String deptId);
}