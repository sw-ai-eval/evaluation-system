package com.eval.domain.competency.service;

import com.eval.domain.competency.dto.CompetencyDTO;
import com.eval.domain.competency.mapper.CompetencyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetencyService {

    private final CompetencyMapper mapper;

    // 1. 역량평가 기준년도 리스트 조회
    public List<Integer> getEvalYears() {
        return mapper.selectEvalYears();
    }

    // 2. 역량평가 목록 조회
    public List<CompetencyDTO.Info> getEvalList(Integer year, String period) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmpNo = auth.getName(); 
        return mapper.selectEvalList(year, period, currentEmpNo);
    }

    // 3. 상세 정보 조회
    public CompetencyDTO.Info getDetail(Integer typeId, String empNo) {
        CompetencyDTO.Info info = mapper.selectCompetencyInfo(typeId, empNo);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmpNo = auth.getName();

        if (info != null) {
            // [방어막 1] 남의 문서인데 아직 본인평가 중이면 문항 자체를 숨김
            if (!empNo.equals(currentEmpNo) && "본인평가중".equals(info.getStatus())) {
                info.setItems(new ArrayList<>()); 
            } else {
                // XML에서 수정한 대로 typeId와 empNo를 넘겨 문항을 가져옴
                List<CompetencyDTO.Item> items = mapper.selectCompetencyItems(typeId, empNo);
                
                // [방어막 2] 1차 평가 중일 때 사원 본인이 조회하면 부서장 의견 가리기
                if ("1차평가대기".equals(info.getStatus()) && empNo.equals(currentEmpNo)) {
                    for (CompetencyDTO.Item item : items) {
                        item.setFirstFeedback(""); 
                        item.setFirstGrade("");    
                    }
                }
                info.setItems(items);
            }
        }
        return info;
    }

    // 4. 역량평가 저장 및 제출
    @Transactional
    public void saveEvaluation(CompetencyDTO.SaveReq req) {
        
        // [본인 역량 평가 저장]
        if ("SELF".equals(req.getStep())) {
            for (CompetencyDTO.Item item : req.getItems()) { 
                // XML 쿼리 조건에 맞춰 empNo, typeId(필요시), item 전달
                mapper.updateSelfAnswer(req.getEmpNo(), item); 
            }
            
            // '제출' 버튼을 눌렀을 때만 상태 변경
            if ("Y".equals(req.getSubCheck())) {
                // 매핑 테이블의 status를 1(1차평가대기)로 변경
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 1); 
            }
            
        // [1차 역량 평가 - 부서장 저장]
        } else if ("FIRST".equals(req.getStep())) {
            for (CompetencyDTO.Item item : req.getItems()) { 
                mapper.updateFirstAnswer(req.getEmpNo(), item); 
            }
            
            // '제출' 버튼을 눌렀을 때만 상태 변경
            if ("Y".equals(req.getSubCheck())) {
                // 매핑 테이블의 status를 2(평가완료)로 변경
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 2); 
            }
        }
    }
}