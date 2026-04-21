package pack.department;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentRepository departmentRepository;


    public DepartmentController(DepartmentService departmentService, DepartmentRepository departmentRepository) {
        this.departmentService = departmentService;
        this.departmentRepository = departmentRepository;
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

    @GetMapping("/department/search")
    public String search_dept() {


        return "redirect:/department";
    }
    
}