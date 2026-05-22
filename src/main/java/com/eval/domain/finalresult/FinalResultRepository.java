package com.eval.domain.finalresult;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eval.domain.employee.Employee;


public interface FinalResultRepository extends JpaRepository<FinalResult, Long> {

	
	Optional<FinalResult> findByEmployeeAndYear(Employee employee, Integer year);

	boolean existsByEmployeeAndYear(Employee e, Integer year);
	
}