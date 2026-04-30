package com.eval.domain.employee.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eval.domain.codetable.CodeService;
import com.eval.domain.dept.Department;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.dept.service.DepartmentService;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.employee.dto.EmpManageDTO;
import com.eval.domain.employee.dto.EmployeeDTO;
import com.eval.domain.employee.mapper.EmployeeMapper;
import com.eval.domain.employee.service.EmployeeService;

import lombok.RequiredArgsConstructor;


@Controller
public class EmployeeController {

	private final DepartmentService departmentService;
	private final EmployeeService employeeService;
	private final CodeService codeService;
	 public EmployeeController(DepartmentService departmentService,EmployeeService employeeService,CodeService codeService,
			 DepartmentRepository departmentRepository
			 ) {
	        this.departmentService = departmentService;
	        this.employeeService=employeeService;
	        this.codeService=codeService;
	    }
	 
	 @GetMapping("/employee")
	 public String employees(Model model,
	                         @RequestParam(required = false) String keyword,
	                         @RequestParam(required = false) String deptId,
	                         @RequestParam(required = false) String status,
	                         @RequestParam(defaultValue = "0") int page,
	                         @RequestParam(defaultValue = "10") int size) {

	     Page<EmpManageDTO> employeePage =
	             employeeService.findEmployees(keyword, deptId, status, page, size);

	     List<EmpManageDTO> employeeList = employeePage.getContent();

	     List<Department> deptList = departmentService.findDepartmentNoDelete();

	     model.addAttribute("employee", new EmpManageDTO());
	     model.addAttribute("employeeList", employeeList);
	     model.addAttribute("employeePage", employeePage); // ⭐ 추가
	     model.addAttribute("deptList", deptList);
	     model.addAttribute("level", codeService.getCodes("POSITION_LEVEL"));
	     model.addAttribute("jobCode", codeService.getCodes("JOB"));
	     
	     System.out.println("LEVEL = " + codeService.getCodes("POSITION_LEVEL"));
	     System.out.println("JOB = " + codeService.getCodes("JOB"));
	     
	     // 검색조건 유지용 ⭐
	     model.addAttribute("keyword", keyword != null ? keyword : "");
	     model.addAttribute("deptId", deptId != null ? deptId : "");
	     model.addAttribute("status", status != null ? status : "");

	     model.addAttribute("pageType", "emp/employee");

	     return "emp/employee";
	 }
	 @GetMapping("/employee/{empNo}")
	 @ResponseBody
	 public EmpManageDTO getEmployee(@PathVariable String empNo) {
	     return employeeService.findByEmpNo(empNo);
	 }
	 

	// 사원 등록 처리
	 @PostMapping("/employee/create")
	 public String createEmployee(@ModelAttribute("employee") EmpManageDTO employee) {

        
	     // 폼 데이터를 처리하여 사원 등록
	    employeeService.createEmp(employee);

	     // 등록 후 사원 목록 페이지로 리다이렉트
	    return "redirect:/employee"; 
	 }
	 
	 //////////////////////////////////////////////////////////////////////  사원 정보 수정
	 @PostMapping("/employee/update")
	 @ResponseBody
	 public ResponseEntity<?> updateEmployee(@RequestBody EmpManageDTO dto) {

	     try {
	         employeeService.updateEmp(dto);
	         return ResponseEntity.ok("SUCCESS");

	     } catch (IllegalArgumentException e) {
	         return ResponseEntity.badRequest().body(e.getMessage());
	     }
	 }
	 
	 
	 
	 
	 
	 
	 
	 
}