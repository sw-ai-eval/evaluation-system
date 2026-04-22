package com.eval.domain.dept.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eval.domain.dept.Department;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, String> {

    // 🔥 부모 ID 기준 조회
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Department> findByParentId(String parentId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Department d WHERE d.id = :id")
    Optional<Department> findByIdForUpdate(String id);
    
    boolean existsByParentId(String parentId);
    
    @Modifying
    @Query("delete from Department d where d.id = :id")
    void deleteByDeptId(@Param("id") String id);
}