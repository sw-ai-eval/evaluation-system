package pack.department;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

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
    public void create(DepartmentDto dto) {

        Department dept = new Department();

        dept.setName(dto.getName());
        dept.setParentId(dto.getParentId());
        dept.setUseYn(dto.getUseYn());

        // 🔥 level 자동 계산
        if (dto.getParentId() == null || dto.getParentId().isBlank()) {
            dept.setLevel(0);
        } else {
            Department parent = departmentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 부서가 존재하지 않습니다."));

            dept.setLevel(parent.getLevel() + 1);
        }

        dept.setCreatedAt(LocalDateTime.now());
        dept.setCreatedBy("SYSTEM");

        String newId = generateDepartmentId(dto.getParentId());
        dept.setId(newId);

        departmentRepository.save(dept);
    }
    private String generateDepartmentId(String parentId) {

        // 🔹 루트
        if (parentId == null || parentId.isBlank()) {

            String maxRootId = departmentRepository.findMaxRootId();

            if (maxRootId == null) {
                return "S1000";
            }

            int num = Integer.parseInt(maxRootId.substring(1, 2));
            return "S" + (num + 1) + "000";
        }

        // 🔹 부모 가져오기
        Department parent = departmentRepository.findById(parentId)
                .orElseThrow();

        int parentLevel = parent.getLevel();

        // 🔹 같은 부모의 자식들 조회
        List<Department> children = departmentRepository.findByParentId(parentId);

        // 🔹 증가할 자리 index
        int index = parentLevel + 1;

        // 🔹 첫 자식
        if (children.isEmpty()) {
            return incrementAt(parentId, index);
        }

        // 🔥 핵심: "가장 큰 값" 찾기
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
}