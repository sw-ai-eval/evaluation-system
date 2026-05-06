package com.eval.domain.performance.service;

import com.eval.domain.performance.dto.PerformanceDTO;
import com.eval.domain.performance.mapper.PerformanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceMapper mapper;

    // 1. 기준년도 리스트 조회
    public List<Integer> getEvalYears() {
        return mapper.selectEvalYears();
    }

    // 2. 평가 목록 조회 (Integer typeId -> String period 로 변경!)
    public List<PerformanceDTO.Info> getEvalList(Integer year, String period) {
        return mapper.selectEvalList(year, period);
    }

    // 3. 상세 정보 조회 (기존 로직 유지)
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
        if ("SELF".equals(req.getStep())) {
            // 본인 평가 저장
            for (PerformanceDTO.Item item : req.getItems()) { 
                mapper.updateSelfAnswer(req.getEmpNo(), item); 
            }
            // 상태 변경: 본인평가중(0) -> 1차평가대기(1)
            mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 1); 
            
        } else if ("FIRST".equals(req.getStep())) {
            // 1차 평가(부서장) 저장
            for (PerformanceDTO.Item item : req.getItems()) { 
                mapper.updateFirstAnswer(req.getEmpNo(), item); 
            }
            // 상태 변경: 1차평가대기(1) -> 1차평가완료(2)
            mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 2); 
        }
    }
}