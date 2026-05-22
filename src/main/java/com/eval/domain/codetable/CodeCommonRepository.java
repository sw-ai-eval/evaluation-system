package com.eval.domain.codetable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CodeCommonRepository extends JpaRepository<CodeCommon, Long> {

    List<CodeCommon> findByGroupIdAndIsUseTrueOrderBySortOrder(Long groupId);
    
    List<CodeCommon> findByGroupId(Long groupId);
    
    @Query("SELECT c.name FROM CodeCommon c WHERE c.groupId = :groupId AND c.code = :code")
    String findNameByGroupAndCode(@Param("groupId") Long groupId, @Param("code") String code);
}