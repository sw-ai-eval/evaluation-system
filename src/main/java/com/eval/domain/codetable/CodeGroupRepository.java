package com.eval.domain.codetable;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeGroupRepository extends JpaRepository<CodeGroup, Long> {

    Optional<CodeGroup> findByGroupCode(String groupCode);
}