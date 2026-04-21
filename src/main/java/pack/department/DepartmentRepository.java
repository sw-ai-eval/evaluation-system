package pack.department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, String> {

    // 🔥 부모 ID 기준 조회
    List<Department> findByParentId(String parentId);

    // 🔥 루트 최댓값 조회
    @Query("SELECT MAX(d.id) FROM Department d WHERE d.parentId IS NULL")
    String findMaxRootId();
}