package com.eval.domain.dept.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.dto.DepartmentDto;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.employee.EmployeeRepository;


@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    private EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository =employeeRepository;
    }
    ////////////////////////////////// 삭제하지 않은 부서만 조회
    public List<Department> findDepartmentNoDelete(){
    	return departmentRepository.findByDeleteYn(false);
    }

    ////////////////////////////////// 사용하는 부서만 조회
    public List<Department> findDepartmentUse(){return departmentRepository.findByUseYn(true);}
    
    ////////////////////////////////// 모든 부서 조회 용
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }
    
    //////////////////////////////////// 해당 아이디 조회
    public Department findById(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("부서 없음"));
    }
    
    public List<Department> getDepartmentTree() {
        List<Department> list = departmentRepository.findByDeleteYn(false);
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

            if (d.getParent() == null) {
                root.add(d);
            } else {

                Department parent = map.get(d.getParent().getId());

                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(d);
                } else {
                    root.add(d);
                }
            }
        }

        return root;
    }
    ////////////////////////////////////////////////////////부서 생성
    @Transactional
    public void create(DepartmentDto dto) {

        Department dept = new Department();

        dept.setName(dto.getName());
        dept.setUseYn(dto.isUseYn());

        Department parent = null;

        if (dto.getParentId() != null && !dto.getParentId().isBlank()) {
            parent = departmentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 부서 없음"));

            dept.setParent(parent);
            dept.setLevel(parent.getLevel() + 1);
        } else {
            dept.setLevel(0);
            dept.setParent(null);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNo = auth.getName();

        dept.setCreatedAt(LocalDateTime.now());
        dept.setCreatedBy(empNo);
        dept.setDeleteYn(false);

        String newId = generateDepartmentId(dto.getParentId());
        dept.setId(newId);

        departmentRepository.save(dept);
    }
    private String generateDepartmentId(String parentId) {

        if (parentId == null || parentId.isBlank()) {
            parentId = "ROOT";
        }

        Department parent = departmentRepository.findById(parentId)
                .orElseThrow();

        int parentLevel = parent.getLevel();

        List<Department> children = departmentRepository.findByParent_Id(parentId);

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

        Department dept = departmentRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("부서 없음"));

        if (dto.getId().equals(dto.getParentId())) {
            throw new IllegalArgumentException("자기 자신을 부모로 지정할 수 없음");
        }

        if (isCircular(dto.getId(), dto.getParentId())) {
            throw new IllegalArgumentException("부모 부서 설정 오류 (순환 구조)");
        }
        ///////////////////////////////////////////////////////////////////////////////////
        /// 
        /// 
        boolean oldUseYn = dept.isUseYn();
        boolean newUseYn = dto.isUseYn();
        
        String oldLeader = dept.getLeaderEmpNo();
        String newLeader = dto.getLeaderEmpNo();
        
        // 🔥 미사용 -> 사용으로 변경할 때만 상위(직계) 체크
	     // 부서 상태 변경 시 상위 부서 체크
     // 🔥 미사용 -> 사용으로 변경할 때만 상위 체크
        if (dto.isUseYn()) {

            Department currentDept = departmentRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("부서 없음"));

            if (currentDept.getParent() != null) {

                Department parentDept = departmentRepository.findById(currentDept.getParent().getId())
                        .orElseThrow(() -> new IllegalArgumentException("상위 부서 없음"));

                // 부모가 미사용이면 차단
                if (!parentDept.isUseYn()) {
                    throw new IllegalArgumentException("상위 부서가 미사용 상태라 현재 부서를 사용으로 변경할 수 없습니다.");
                }
            }
        }
        

        if (newLeader == null || newLeader.isBlank()) {
            newLeader = null;
        } else {
            boolean exists = employeeRepository.existsById(newLeader);
            if (!exists) {
                throw new IllegalArgumentException("존재하지 않는 사원입니다.");
            }
        }

        dept.setLeaderEmpNo(newLeader);
        
        dept.setName(dto.getName());
        dept.setLeaderEmpNo(newLeader);
        dept.setUseYn(dto.isUseYn());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNo = auth.getName();

        dept.setUpdatedAt(LocalDateTime.now());
        dept.setUpdatedBy(empNo);
        
        // 🔥 사용 -> 미사용일 때만 하위 전파
        if (oldUseYn && !newUseYn) {

            List<String> childIds = getAllChildIds(dto.getId());
            childIds.add(dto.getId()); // 자기 포함

            departmentRepository.disableAllByIds(childIds);
        }
        
        applyLeaderChange(oldLeader, newLeader);
    }
    private boolean isCircular(String deptId, String newParentId) {

        if (newParentId == null) return false;

        if (deptId.equals(newParentId)) return true;

        Department parent = departmentRepository.findById(newParentId)
                .orElse(null);

        while (parent != null) {

            Department next = parent.getParent();

            if (next == null) return false;

            if (next.getId().equals(deptId)) {
                return true;
            }

            parent = next;
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
    ////////////////////////////////////////////////////////////// 부서 검색
    
    public List<Department> search(String name, Boolean useYn) {
        return departmentRepository.search(name, useYn);
    }
    ///////////////////////////////////////////////////////////////// 하위 부서 미사용 처리
    /// 
    public List<String> getAllChildIds(String parentId) {
    	List<String> result = new ArrayList<>();
	    collect(parentId, result);
	    return result;
	}
	
	private void collect(String parentId, List<String> result) {
	
	    List<String> children = departmentRepository.findChildIds(parentId);
	
	    for (String childId : children) {
	        result.add(childId);
	        collect(childId, result); // 🔁 계속 내려감
	    }
	}
	//////////////////////////////////////////////////////////////////// 부서장 변경시 전 부서장으로 부서원으로
	private void applyLeaderChange(String oldLeader, String newLeader) {

	    if (Objects.equals(oldLeader, newLeader)) return;

	    // 기존 부서장 → 부서원
	    if (oldLeader != null) {
	        employeeRepository.updatePosition(oldLeader, "부서원");
	    }

	    // 신규 부서장 → 부서장
	    if (newLeader != null) {
	        employeeRepository.updatePosition(newLeader, "부서장");
	    }
	}
}