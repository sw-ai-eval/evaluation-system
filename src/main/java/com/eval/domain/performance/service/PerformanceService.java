package com.eval.domain.performance.service;

import com.eval.domain.performance.dto.PerformanceDTO;
import com.eval.domain.performance.mapper.PerformanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceMapper mapper;

    // 1. 기준년도 리스트 조회
    public List<Integer> getEvalYears() {
        return mapper.selectEvalYears();
    }

    // ====================================================================
    // 2. 평가 목록 조회
    // ====================================================================
    public List<PerformanceDTO.Info> getEvalList(Integer year, String period) {
        // 현재 로그인한 사원의 사번 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmpNo = auth.getName(); 
        
        return mapper.selectEvalList(year, period, currentEmpNo);
    }

    // 3. 상세 정보 조회
    public PerformanceDTO.Info getEvalDetail(Integer typeId, String empNo) {
        PerformanceDTO.Info info = mapper.selectEvalInfo(typeId, empNo);
        if (info != null) {
            List<PerformanceDTO.Item> items = mapper.selectEvalItems(typeId, empNo);
            info.setItems(items);
        }
        return info;
    }

    // 4. 본인/1차 평가 저장 및 상태 변경
    @Transactional
    public void saveEvaluation(PerformanceDTO.SaveReq req) {
        
        // [본인 평가] 처리 흐름 (화면에서 "SELF"로 넘어옴 -> DB에는 step=0으로 반영)
        if ("SELF".equals(req.getStep())) {

            for (PerformanceDTO.Item item : req.getItems()) { 
                mapper.updateSelfAnswer(req.getEmpNo(), item); 
            }
            
            if ("Y".equals(req.getIsSubmit())) {
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 1); 
            }
            
        // [1차 평가] 처리 흐름 (화면에서 "FIRST"로 넘어옴 -> DB에는 step=1로 반영)
        } else if ("FIRST".equals(req.getStep())) {
            
            for (PerformanceDTO.Item item : req.getItems()) { 
                mapper.updateFirstAnswer(req.getEmpNo(), item); 
            }
            
            if ("Y".equals(req.getIsSubmit())) {
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 2); 
            }
        }
    }
}