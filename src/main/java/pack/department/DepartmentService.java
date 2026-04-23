package pack.department;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import pack.employee.EmployeeRepository;


@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    private EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository =employeeRepository;
    }
    
    public Department findById(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("부서 없음"));
    }
    
    public List<Department> getDepartmentTree() {
        List<Department> list = departmentRepository.findAll();
        return buildTree(list);
    }

    private List<Department> buildTree(List<Department> list) {
        Map<String, Department> map = new HashMap<>();
        List<Department> root = new ArrayList<>();

        for (Department d : list) {
            d.setChildren(new ArrayList<>());
            map.put(d.getId(), d);
        }

        for (Department d : list) {
            if (d.getParentId() == null) {
                root.add(d);
            } else {
                Department parent = map.get(d.getParentId());
                parent.getChildren().add(d);
            }
        }

        return root;
    }
    ////////////////////////////////////////////////////////부서 생성
    @Transactional
    public void create(DepartmentDto dto) {

        Department dept = new Department();

        dept.setName(dto.getName());
        dept.setParentId(dto.getParentId());
        dept.setUseYn(dto.getUseYn());

        Department parent = null;

        // 🔥 부모 락 획득
        if (dto.getParentId() != null && !dto.getParentId().isBlank()) {
            parent = departmentRepository.findByIdForUpdate(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 부서 없음"));

            dept.setLevel(parent.getLevel() + 1);
        } else {
            dept.setLevel(0);
        }

        dept.setCreatedAt(LocalDateTime.now());
        dept.setCreatedBy("SYSTEM");

        // 🔥 여기서 ID 생성 (락 안에서 실행됨)
        String newId = generateDepartmentId(dto.getParentId());
        dept.setId(newId);

        departmentRepository.save(dept);
    }
    private String generateDepartmentId(String parentId) {

        // 🔥 ROOT 통일
        if (parentId == null || parentId.isBlank()) {
            parentId = "ROOT";
        }

        // 🔹 부모 가져오기 (락 걸린 메서드 쓰는 게 좋음)
        Department parent = departmentRepository.findById(parentId)
                .orElseThrow();

        int parentLevel = parent.getLevel();

        // 🔹 같은 부모 자식 조회 (여기도 락)
        List<Department> children = departmentRepository.findByParentId(parentId);

        int index = parentLevel + 1;

        if (children.isEmpty()) {
            return incrementAt(parentId, index);
        }

        String maxId = children.stream()
                .map(Department::getId)
                .max(String::compareTo)
                .orElse(parentId);

        return incrementAt(maxId, index);
    }
    private String incrementAt(String id, int index) {

        char[] chars = id.toCharArray();

        int num = chars[index] - '0';
        num += 1;

        if (num > 9) {
            throw new RuntimeException("ID 자리 초과 (설계 변경 필요)");
        }

        chars[index] = (char) ('0' + num);

        // 뒤는 0으로 초기화
        for (int i = index + 1; i < chars.length; i++) {
            chars[i] = '0';
        }

        return new String(chars);
    }
    @Transactional
    public void createWithRetry(DepartmentDto dto) {

        int retry = 0;

        while (retry < 3) {
            try {
                create(dto);
                return;
            } catch (DataIntegrityViolationException e) {
                retry++;
            }
        }

        throw new RuntimeException("부서 생성 실패 (재시도 초과)");
    }
    //////////////////////////////////////////////////////////////////////////// 부서 수정
    @Transactional
    public void update(DepartmentDto dto) {

        Department dept = departmentRepository.findById(dto.getId()).orElseThrow(() -> new IllegalArgumentException("부서 없음"));

        // 1) 순환 체크
        if (isCircular(dto.getId(), dto.getParentId())) {
            throw new IllegalArgumentException("부모 부서 설정 오류 (순환 구조)");
        }

        // 2) 자기 자신을 부모로 못 가게
        if (dto.getId().equals(dto.getParentId())) {
            throw new IllegalArgumentException("자기 자신을 부모로 지정할 수 없음");
        }
        
        if (dto.getLeaderEmpNo() == null || dto.getLeaderEmpNo().toString().isBlank()) {
            dto.setLeaderEmpNo(null);
        }

        // 3) 실제 업데이트
        dept.setName(dto.getName());
        dept.setLeaderEmpNo(dto.getLeaderEmpNo());
        dept.setParentId(dto.getParentId());
        dept.setUseYn(dto.getUseYn());
    }
    private boolean isCircular(String deptId, String newParentId) {
        if (newParentId == null) return false;

        if (deptId.equals(newParentId)) return true;

        Department parent = departmentRepository.findById(newParentId)
            .orElse(null);

        while (parent != null) {
            if (parent.getParentId() == null) return false;

            if (parent.getParentId().equals(deptId)) {
                return true; // 자기 자신으로 돌아옴 → 순환
            }

            parent = departmentRepository.findById(parent.getParentId())
                .orElse(null);
        }

        return false;
    }
    //////////////////////////////////////////////////////////// 부서 삭제
    public boolean hasEmployees(String deptId) {
    	return employeeRepository.existsByDeptId(deptId); // 부서 소속원 조회
    }
    public boolean hasChildernDept(String deptId) {
    	return departmentRepository.existsByParentId(deptId); // 하위 부서 조회
    }
    
    @Transactional
    public void delete(String deptId) {
    	departmentRepository.deleteByDeptId(deptId);
    }
}