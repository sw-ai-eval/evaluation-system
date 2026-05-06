package com.eval.domain.employee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
	List<Employee> findByDeptId(String deptId);
	
	
	boolean existsByDeptId(String deptId);
	
	
	List<Employee> findByDeptIdAndStatus(String deptId, String status);
	
	@Query(value = "SELECT NEXT VALUE FOR dbo.emp_seq_52", nativeQuery = true) // 사번 증가 
	Long getNextEmpNo();
    
    @Modifying
    @Query("update Employee e set e.position = :position where e.empNo = :empNo")
    void updatePosition(@Param("empNo") String empNo,
                        @Param("position") String position);
    
    @Query("SELECT d.leaderEmpNo FROM Department d WHERE d.id = :deptId")
    String findDeptLeader(@Param("deptId") String deptId);
    
    
    List<Employee> findByLevelId(int levelId);
}