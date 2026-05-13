package com.eval.domain.performance.service;

import com.eval.domain.performance.dto.PerformanceDTO;
import com.eval.domain.performance.mapper.PerformanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
    String currentEmpNo = auth.getName(); 
    
    return mapper.selectEvalList(year, period, currentEmpNo);
}

public PerformanceDTO.Info getEvalDetail(Integer typeId, String empNo) {
    PerformanceDTO.Info info = mapper.selectEvalInfo(typeId, empNo);
    
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentEmpNo = auth.getName();

    if (info != null) {
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
            mapper.updateSelfAnswer(req.getEmpNo(), item); 
        }
        
        if ("Y".equals(req.getSubCheck())) {
            mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 1); 
        }
        
    } else if ("FIRST".equals(req.getStep())) {
        for (PerformanceDTO.Item item : req.getItems()) { 
            mapper.updateFirstAnswer(req.getEmpNo(), item); 
        }
        
        if ("Y".equals(req.getSubCheck())) {
            mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 2); 
        }
    }
}
}