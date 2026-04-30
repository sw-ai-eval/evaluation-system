package com.eval.domain.codetable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeCommonRepository extends JpaRepository<CodeCommon, Long> {

    List<CodeCommon> findByGroupIdAndIsUseTrueOrderBySortOrder(Long groupId);
}