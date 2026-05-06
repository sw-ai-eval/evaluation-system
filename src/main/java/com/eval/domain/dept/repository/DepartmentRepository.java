package com.eval.domain.dept.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.dto.DepartmentDto;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, String> {

    // 🔥 부모 ID 기준 조회
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Department> findByParent_Id(String parentId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Department d WHERE d.id = :id")
    Optional<Department> findByIdForUpdate(String id);
    
    boolean existsByParentId(String parentId);

    @Modifying
    @Transactional
    @Query("update Department d set d.deleteYn = true, d.useYn = false where d.id = :id")
    void deleteByDeptId(@Param("id") String id);
    
    @Query("""
    	    SELECT d FROM Department d
    	    WHERE d.deleteYn = false
    	    AND (:name IS NULL OR :name = ''
    	         OR d.id LIKE %:name%
    	         OR d.name LIKE %:name%)
    	    AND (:useYn IS NULL OR d.useYn = :useYn)
    	""")
    	List<Department> search(@Param("name") String name,
    	                        @Param("useYn") Boolean useYn);
    
    List<Department> findByDeleteYn(boolean deleteYn);
    
    @Query("SELECT d.id FROM Department d WHERE d.parent.id = :parentId AND d.deleteYn = false")
    List<String> findChildIds(@Param("parentId") String parentId);
    
    @Modifying
    @Query("UPDATE Department d SET d.useYn = false WHERE d.id IN :ids")
    void disableAllByIds(@Param("ids") List<String> ids);

    List<Department> findByUseYn(boolean b);
}