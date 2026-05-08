package com.eval.domain.employee;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
	
	// 부서에 소속된 모든 사원 검색
	List<Employee> findByDeptId(String deptId);
	
	List<Employee> findByDeptIdIn(List<String> deptIds);
	
	// 부서 존재 여부
	boolean existsByDeptId(String deptId);
	
	// 소속된 부서에 재직 상태에 따른 사원 검색
	List<Employee> findByDeptIdAndStatus(String deptId, String status);
	
	List<Employee> findByDeptIdInAndStatusNot(List<String> deptIds, String status);
	
	@Query(value = "SELECT NEXT VALUE FOR dbo.emp_seq_52", nativeQuery = true) // 사번 증가 
	Long getNextEmpNo();
    
    @Modifying
    @Query("update Employee e set e.position = :position where e.empNo = :empNo")
    void updatePosition(@Param("empNo") String empNo,
                        @Param("position") String position);
    
    @Query("SELECT d.leaderEmpNo FROM Department d WHERE d.id = :deptId")
    String findDeptLeader(@Param("deptId") String deptId);
    
    List<Employee> findByLevelId(int levelId);
    
    List<Employee> findByEmpNoIn(Collection<String> empNos);
    
    // 부서에 소속된 사원 이름으로 정렬
    List<Employee> findByDeptIdOrderByNameAsc(String deptId);
    
    // 사원 1명 조회
    Employee findByEmpNo(String empNo);

}