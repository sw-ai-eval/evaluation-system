package pack.employee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
	List<Employee> findByDeptId(String deptId);
	
	boolean existsByDeptId(String deptId);

}