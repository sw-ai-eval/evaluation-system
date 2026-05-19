package com.eval.domain.performance.service;

import com.eval.domain.performance.dto.PerformanceDTO;
import com.eval.domain.performance.mapper.PerformanceMapper;
import com.eval.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceMapper mapper;

    public List<Integer> getEvalYears() {
        return mapper.selectEvalYears();
    }

    public List<PerformanceDTO.Info> getEvalList(Integer year, String period) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();

        // ADMIN이면 전체 조회 (empNo = null → WHERE 조건 없음)
        if ("ADMIN".equals(userDetails.getRole())) {
            return mapper.selectEvalList(year, period, null);
        }
        return mapper.selectEvalList(year, period, currentEmpNo);
    }

    public List<PerformanceDTO.Info> getConfirmedList(Integer year, String period) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();

        if ("ADMIN".equals(userDetails.getRole())) {
            return mapper.selectConfirmedList(year, period, null);
        }
        return mapper.selectConfirmedList(year, period, currentEmpNo);
    }

    public PerformanceDTO.Info getEvalDetail(Integer typeId, String empNo) {
        PerformanceDTO.Info info = mapper.selectEvalInfo(typeId, empNo);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();

        if (info != null) {
            // ADMIN은 모든 내용 열람 가능
            if ("ADMIN".equals(userDetails.getRole())) {
                info.setItems(mapper.selectEvalItems(typeId, empNo));
                return info;
            }

            if (!empNo.equals(currentEmpNo) && "본인평가중".equals(info.getStatus())) {
                info.setItems(new ArrayList<>());
            } else {
                List<PerformanceDTO.Item> items = mapper.selectEvalItems(typeId, empNo);
                if ("1차평가대기".equals(info.getStatus()) && empNo.equals(currentEmpNo)) {
                    for (PerformanceDTO.Item item : items) {
                        item.setFirstFeedback("");
                        item.setFirstScore(null);
                    }
                }
                info.setItems(items);
            }
        }
        return info;
    }

    @Transactional
    public void saveEvaluation(PerformanceDTO.SaveReq req) {
        if ("SELF".equals(req.getStep())) {
            for (PerformanceDTO.Item item : req.getItems()) {
                mapper.updateSelfAnswer(req.getEmpNo(), req.getTypeId(), item);
            }
            if ("Y".equals(req.getSubCheck())) {
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 1, 0);
            }
        } else if ("FIRST".equals(req.getStep())) {
            for (PerformanceDTO.Item item : req.getItems()) {
                mapper.updateFirstAnswer(req.getEmpNo(), req.getTypeId(), item);
            }
            if ("Y".equals(req.getSubCheck())) {
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 2, 0);
            }
        }
    }
}