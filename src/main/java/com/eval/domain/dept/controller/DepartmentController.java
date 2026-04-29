package com.eval.domain.dept.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.dto.DepartmentDto;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.dept.service.DepartmentService;

import  com.eval.domain.employee.Employee;
import com.eval.domain.employee.EmployeeRepository;


@Controller
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentController(DepartmentService departmentService, DepartmentRepository departmentRepository,EmployeeRepository employeeRepository) {
        this.departmentService = departmentService;
        this.departmentRepository = departmentRepository;
        this.employeeRepository=employeeRepository;
    }

    // 부서관리 페이지
    @GetMapping("/department")
    public String departments(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String useYn,
            @RequestParam(required = false) String editId, // ⭐ 추가
            Model model
    ) {

        List<Department> tree = departmentService.getDepartmentTree();


        Boolean useYnBool = null;
        if ("true".equals(useYn)) useYnBool = true;
        else if ("false".equals(useYn)) useYnBool = false;

        List<Department> list;

        if ((name != null && !name.isEmpty()) || useYnBool != null) {
            list = departmentService.search(name, useYnBool);
        } else {
            list = departmentRepository.findByDeleteYn(false);
        }

        // ⭐ 전체 리스트
        List<Department> deptList = departmentRepository.findByDeleteYn(false);

        // ⭐ 수정 시 자기 자신 제외 리스트
        List<Department> parentList = deptList;

        if (editId != null) {
            parentList = deptList.stream()
                    .filter(d -> !d.getId().equals(editId))
                    .toList();
        }

        model.addAttribute("deptTree", tree);
        model.addAttribute("deptList", deptList);
        model.addAttribute("parentList", parentList); // ⭐ 핵심

        model.addAttribute("list", list);
        model.addAttribute("name", name);
        model.addAttribute("useYn", useYn);
        model.addAttribute("editId", editId); // ⭐ 중요


        model.addAttribute("pageType", "dept/department");// css 
        

        List<Employee> empList = employeeRepository.findAll(); // 혜나가추가
        model.addAttribute("empList", empList);

        

        return "dept/department";
    }
    
    // 부서 생성 매핑
    @PostMapping("/department/create")
    public String create_dept(@ModelAttribute DepartmentDto dto) {

        departmentService.create(dto);

        return "redirect:/department";
    }

    //부서 수정 메핑
    @PostMapping("/department/update")
    @ResponseBody
    public Map<String, Object> update_dept(@ModelAttribute DepartmentDto dto) {

        Map<String, Object> result = new HashMap<>();
        
        String leader = dto.getLeaderEmpNo();

        if (leader == null || leader.isBlank()) {
            dto.setLeaderEmpNo(null);
        }
        
        try {
            departmentService.update(dto);

            result.put("success", true);
            result.put("message", "수정 완료");

        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }
    
    // 부서장 지정하기 위한 부서원 리스트 가져오기
    @GetMapping("/department/{id}/employees")
    @ResponseBody
    public List<Employee> getEmployees(@PathVariable String id) {
        return employeeRepository.findByDeptIdAndStatusNot(id, "RESIGNED");
    }
    
    @PostMapping("/department/{id}/delete")
    @ResponseBody
    public String delete(@PathVariable String id) {

        if (departmentService.hasEmployees(id)) {
            return "소속된 부서원이 존재합니다. 미사용으로 변경해주세요";
        }
        if(departmentService.hasChildernDept(id)) {
        	return "하위 부서가 존재합니다. 하위 부서 이동 또는 삭제해주세요";
        }

        departmentService.delete(id);
        return "삭제 완료";
    }
    
   
    
}