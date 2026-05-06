package com.eval.domain.codetable;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeGroupRepository groupRepo;
    private final CodeCommonRepository commonRepo;

    public List<CodeCommon> getCodes(String groupCode) {
        CodeGroup group = groupRepo.findByGroupCode(groupCode)
                .orElseThrow(() -> new RuntimeException("그룹 없음"));

        return commonRepo.findByGroupIdAndIsUseTrueOrderBySortOrder(group.getId());
    }
}