package pack.department;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import pack.employee.Employee;
import pack.employee.EmployeeRepository;


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

    @GetMapping("/department")
    public String departments(Model model) {

        List<Department> tree = departmentService.getDepartmentTree();
        List<Department> list = departmentRepository.findAll();

        model.addAttribute("deptTree", tree);
        model.addAttribute("deptList", list);

        return "department";
    }
    
    @PostMapping("/department/create")
    public String create_dept(@ModelAttribute DepartmentDto dto) {

        departmentService.create(dto);

        return "redirect:/department";
    }

    @PostMapping("/department/update")
    public String update_dept(@ModelAttribute DepartmentDto dto) {
    	
    	departmentService.update(dto);
    	
    	return "redirect:/department";
    }
    
    // 부서장 지정하기 위한 부서원 리스트 가져오기
    @GetMapping("/department/{id}/employees")
    @ResponseBody
    public List<Employee> getEmployees(@PathVariable String id) {
        return employeeRepository.findByDeptId(id);
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
    
    
    //검색 필터
    @GetMapping("/department/search")
    public String search_dept() {
    	
    	

        return "redirect:/department";
    }
    
}