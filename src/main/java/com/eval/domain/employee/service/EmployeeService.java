package com.eval.domain.employee.service;

import com.eval.domain.dept.Department;
import com.eval.domain.dept.repository.DepartmentRepository;
import com.eval.domain.employee.Employee;
import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.employee.dto.EmpManageDTO;
import com.eval.domain.employee.dto.EmployeeDTO;
import com.eval.domain.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    
    public EmployeeService(EmployeeMapper employeeMapper,
            PasswordEncoder passwordEncoder,
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository) {
	this.employeeMapper = employeeMapper;
	this.passwordEncoder = passwordEncoder;
	this.employeeRepository = employeeRepository;
	this.departmentRepository=departmentRepository;
	}

    public List<Employee> getEmployeesByDept(String deptId) {
        List<String> allDeptIds = new java.util.ArrayList<>();
        getAllSubDepartmentIds(deptId, allDeptIds);
        
        // 하위 부서원까지 싹 다 조회 (단, 퇴사자는 제외)
        return employeeRepository.findByDeptIdInAndStatusNot(allDeptIds, "RESIGNED");
    }

    private void getAllSubDepartmentIds(String parentId, List<String> list) {
        list.add(parentId); 
        List<String> childIds = departmentRepository.findChildIds(parentId);
        
        if (childIds != null && !childIds.isEmpty()) {
            for (String childId : childIds) {
                getAllSubDepartmentIds(childId, list); 
            }
        }
    }

    /**
     * 신규 사원 등록
     */
    @Transactional
    public void registerEmployee(EmployeeDTO dto) {
        // 1. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(encodedPassword);
        
        // 2. DB 저장 실행 (주석 해제 후 Mapper와 연결 필요)
        // employeeMapper.insertEmployee(dto); 
    }

    /**
     * 관리자: 비밀번호 초기화 ('1234') 및 잠금 해제
     */
    @Transactional
    public void resetEmployeePassword(String targetEmpNo) {
        String defaultPassword = "1234";
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        
        // Mapper의 resetPasswordAndUnlock 메서드 호출
        employeeMapper.resetPasswordAndUnlock(targetEmpNo, encodedPassword);
    }

    /**
     * 로그인 실패 횟수 증가 및 자동 잠금 로직
     */
    @Transactional
    public int increaseFailCount(String empNo) {
        employeeMapper.incrementFailCount(empNo);
        int currentFailCount = employeeMapper.getFailCount(empNo);
        
        // 5회 이상이면 DB의 'locked' 컬럼을 1로 업데이트
        if (currentFailCount >= 5) {
            employeeMapper.updateLockedStatus(empNo);
        }
        
        return currentFailCount;
    }
    
    @Transactional
    public void forceLock(String empNo) {
        employeeMapper.updateLockedStatus(empNo);
    }
    
    /**
     * 실패 횟수 초기화 (로그인 성공 시 호출)
     */
    @Transactional
    public void resetFailCount(String empNo) {
        employeeMapper.resetFailCount(empNo);
    }
    
    // 전체 사원 목록 조회
    public List<EmployeeDTO> getAllEmployees() {
        return employeeMapper.findAll();
    }
    
    public Page<EmpManageDTO> findEmployees(String keyword, String deptId, String status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        int offset = page * size;

        List<EmpManageDTO> list =
                employeeMapper.search(keyword, deptId, status, offset, size);

        int total =
                employeeMapper.countEmployees(keyword, deptId, status);

        return new PageImpl<>(list, pageable, total);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    public EmpManageDTO findByEmpNo(String empNo) {
        return employeeMapper.findByEmpNoDetail(empNo);
    }
    
    
    public void createEmp(EmpManageDTO employeeDTO) {

        // DTO를 엔티티로 변환
        Employee employee = new Employee();
        
        String empNo = generateEmpNo();
        
        
        // 관리자 사번 --> 생성자 표시용
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminNo = auth.getName();

        employee.setCreatedAt(LocalDateTime.now());
        employee.setCreatedBy(adminNo); 
        employee.setResignDate(null);   // 퇴사일 제외
	    employee.setPassword("$2a$10$kBio8K0mGAXpAAJ9o3XWNuJlDSNd7DxN4CgD.jOsLDFKyBSt9rK0u");     //  초기 비밀번호 123
	    
	    if(employeeDTO.getLevelId()==6) {
	    	employee.setPosition("임원");
	    }else {
	    	employee.setPosition("부서원");
	    }
        
        employee.setEmpNo(empNo); // 사번 자동 증가 로직 구현해야 함----------------------
        employee.setName(employeeDTO.getName());
        employee.setDeptId(employeeDTO.getDeptId());
        employee.setStatus(employeeDTO.getStatus());
        employee.setRole(employeeDTO.getRole());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhone());
        employee.setLevelId(employeeDTO.getLevelId());
        employee.setJobId(employeeDTO.getJobId());
        employee.setHireDate(employeeDTO.getHireDate());

        // 기본값 설정 (필요 시)
        employee.setFailCount(0);
        employee.setLocked(0);

        // 엔티티 저장
        employeeRepository.save(employee);
    }
    
    private String generateEmpNo() { ////////////////////////////////////// 사번 자동 증가 로직 구현 
    	Long next = employeeRepository.getNextEmpNo();
        return String.valueOf(next);
    }
    
    
    @Transactional
    public void updateEmp(EmpManageDTO dto) { ///////////////////////////////////////////// 사원 수정

        Employee employee = employeeRepository.findById(dto.getEmpNo())
                .orElseThrow(() -> new IllegalArgumentException("사원 없음"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminNo = auth.getName();

        boolean deptChanged =employee.getDeptId() != null &&dto.getDeptId() != null && !employee.getDeptId().equals(dto.getDeptId());

        // 부서장 부서 이동 제한
        if ("부서장".equals(employee.getPosition()) && deptChanged) {
            throw new IllegalArgumentException("부서장은 부서 이동 대상에서 제외됩니다. \n리더 권한 이양 후 진행해 주세요.");
        }

        boolean toExecutive = dto.getLevelId() == 6;
        boolean wantResigned = "RESIGNED".equals(dto.getStatus());
        boolean isLeader = "부서장".equals(employee.getPosition());


        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDeptId(dto.getDeptId());
        employee.setLevelId(dto.getLevelId());
        employee.setJobId(dto.getJobId());
        employee.setHireDate(dto.getHireDate());
        employee.setResignDate(dto.getResignDate());
        employee.setRole(dto.getRole());

        employee.setUpdatedBy(adminNo);
        employee.setUpdatedAt(LocalDateTime.now());
        
        if("임원".equals(employee.getPosition())&&dto.getLevelId()!=6) {
        	employee.setPosition("부서원");
        }


        boolean isResignDateValid =dto.getResignDate() != null &&!dto.getResignDate().isAfter(LocalDate.now());

        if (wantResigned && !isResignDateValid) {
            throw new IllegalArgumentException("퇴직일이 아직 도래하지 않았습니다.");
        }

        if (wantResigned && isResignDateValid) {
            employee.setStatus("RESIGNED");
            employee.setLocked(1);
            employee.setLockedAt(LocalDateTime.now());

            removeDepartmentLeaderIfNeeded(employee, adminNo);
        } else {
            employee.setStatus(dto.getStatus());
        }


        // 승진 처리 (임원)
        if (toExecutive) {
            employee.setPosition("임원");

            removeDepartmentLeaderIfNeeded(employee, adminNo);
        }


        if (employee.getPosition() == null) {
            employee.setPosition("부서원");
        }


        if (employee.getCreatedAt() == null) {
            employee.setCreatedAt(LocalDateTime.now());
        }

        if (employee.getCreatedBy() == null) {
            employee.setCreatedBy(adminNo);
        }
    }
    
    private void removeDepartmentLeaderIfNeeded(Employee employee, String adminNo) {

        if (!"부서장".equals(employee.getPosition())) return;
        if (employee.getDeptId() == null) return;

        Department department = departmentRepository
                .findById(employee.getDeptId())
                .orElseThrow(() -> new IllegalArgumentException("부서 없음"));

        if (employee.getEmpNo() != null &&
            employee.getEmpNo().equals(department.getLeaderEmpNo())) {

            department.setLeaderEmpNo(null);
            department.setUpdatedBy(adminNo);
            department.setUpdatedAt(LocalDateTime.now());
        }
    }
    
    
    
    public List<Employee> findExecutive() {
        return employeeRepository.findByLevelId(6);
    }
    

    // empNo 기준 같은 부서, ACTIVE 상태, 특정 직책 사원 조회
    public List<Employee> getActiveColleaguesByEmpNoAndPosition(String empNo, String position) {
        // 1. 사원 조회
        Employee emp = employeeRepository.findByEmpNo(empNo);
        if (emp == null) {
            return List.of(); // 사원이 없으면 빈 리스트 반환
        }

        // 2. 같은 부서의 ACTIVE 상태, 특정 직책 사원 조회
        return employeeRepository.findByDeptIdAndStatusAndPosition(emp.getDeptId(), "ACTIVE", position);
    }
    

    public String getExecutivesEmpNo(String deptId) {

        String currentDeptId = deptId;

        while (currentDeptId != null) {

            // 현재 부서 임원 조회
            String empNo =employeeMapper.findExecutiveEmpNoByDeptId(currentDeptId);

            if (empNo != null) {
                return empNo;
            }

            // 부서 조회
            Department dept = departmentRepository
                    .findById(currentDeptId)
                    .orElse(null);

            if (dept == null) {
                return null;
            }

            // 상위 부서 이동
            currentDeptId = dept.getParentId();
        }

        return null;
    }

}