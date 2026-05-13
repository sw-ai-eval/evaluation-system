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
            if (!empNo.equals(currentEmpNo) && "본인평가중".equals(info.getStatus())) {
                info.setItems(new ArrayList<>());
            } else {
                List<CompetencyDTO.Item> items = mapper.selectCompetencyItems(typeId, empNo);

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

        if ("SELF".equals(req.getStep())) {
            for (CompetencyDTO.Item item : req.getItems()) {
                mapper.updateSelfAnswer(req.getEmpNo(), req.getTypeId(), item);
            }
            if ("Y".equals(req.getSubCheck())) {
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 1);
            }

        } else if ("FIRST".equals(req.getStep())) {
            // step=1 매핑 레코드 없으면 먼저 생성
            if (mapper.existsFirstMapping(req.getEmpNo(), req.getTypeId()) == 0) {
                mapper.insertFirstMapping(req.getEmpNo(), req.getTypeId());
            }
            for (CompetencyDTO.Item item : req.getItems()) {
                mapper.updateFirstAnswer(req.getEmpNo(), req.getTypeId(), item);
            }
            if ("Y".equals(req.getSubCheck())) {
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 2);
            }
        }
    }
}