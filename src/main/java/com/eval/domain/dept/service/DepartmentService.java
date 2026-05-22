package com.eval.domain.dept.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
            String parentId = d.getParent() != null ? d.getParent().getId() : null;
            if (parentId == null) {
                root.add(d);
            } else {
                Department parent = map.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(d);
                } else {
                    // 부모를 찾을 수 없는 경우 root로 처리 (혹은 로그)
                    root.add(d);
                    System.out.println("Warning: parent not found for " + d.getName() + ", id=" + d.getId());
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

        String newId;

        if (dto.getParentId() != null && !dto.getParentId().isBlank()) {
            parent = departmentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 부서 없음"));

            dept.setParent(parent);
            dept.setLevel(parent.getLevel() + 1);

            newId = generateTreeDepartmentId(parent.getId());
        } else {
            dept.setLevel(0);
            dept.setParent(null);

            // 루트 부서 ID, 예: S3_00
            newId = generateTreeDepartmentId(null);
        }

        dept.setId(newId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String empNo = auth.getName();

        dept.setCreatedAt(LocalDateTime.now());
        dept.setCreatedBy(empNo);
        dept.setDeleteYn(false);

        departmentRepository.save(dept);
    }

    /**
     * 트리형 ID 생성 (_ 포함)
     */
    private String generateTreeDepartmentId(String parentId) {

        if (parentId == null || parentId.isBlank()) {
            // 루트 부서 처리
            List<Department> roots = departmentRepository.findByParent_Id(null); // 루트 부서만 가져오기
            int nextRootNum = 0;

            if (!roots.isEmpty()) {
                // 기존 루트 부서에서 가장 큰 숫자 추출
                nextRootNum = roots.stream()
                        .map(Department::getId)
                        .map(id -> {
                            // 루트 부서는 "S숫자" 형식
                            try {
                                return Integer.parseInt(id.replaceAll("[^0-9]", ""));
                            } catch (NumberFormatException e) {
                                return -1; // 숫자가 안될 경우
                            }
                        })
                        .max(Integer::compareTo)
                        .orElse(-1) + 1; // 다음 루트 번호
            }

            if (nextRootNum > 99) {
                throw new RuntimeException("루트 부서 번호 초과");
            }

            return "S" + nextRootNum;
        } else {
            // 하위 부서 처리
            String parentCode = parentId;

            List<Department> children = departmentRepository.findByParent_Id(parentId);

            int nextSeq = 1;
            if (!children.isEmpty()) {
                Set<Integer> used = children.stream()
                        .map(Department::getId)
                        .map(id -> {
                            int lastUnderscore = id.lastIndexOf('_');
                            String numPart = lastUnderscore == -1 ? id.replaceAll("[^0-9]", "") : id.substring(lastUnderscore + 1);
                            try {
                                return Integer.parseInt(numPart);
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        })
                        .collect(Collectors.toSet());

                while (used.contains(nextSeq)) {
                    nextSeq++;
                }
            }

            if (nextSeq > 99) {
                throw new RuntimeException("ID 자리수 초과 (설계 변경 필요)");
            }

            String childCode = String.format("%02d", nextSeq);
            return parentCode + "_" + childCode;
        }
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