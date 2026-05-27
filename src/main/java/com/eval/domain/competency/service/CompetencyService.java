package com.eval.domain.competency.service;

import com.eval.domain.competency.dto.CompetencyDTO;
import com.eval.domain.competency.mapper.CompetencyMapper;
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
public class CompetencyService {

    private final CompetencyMapper mapper;

    public List<Integer> getEvalYears() {
        return mapper.selectEvalYears();
    }

    public List<CompetencyDTO.Info> getEvalList(Integer year, String period) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();

        // ADMIN이면 전체 조회
        if ("ADMIN".equals(userDetails.getRole())) {
            return mapper.selectEvalList(year, period, null);
        }
        return mapper.selectEvalList(year, period, currentEmpNo);
    }

    public List<CompetencyDTO.Info> getConfirmedList(Integer year, String period) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();

        if ("ADMIN".equals(userDetails.getRole())) {
            return mapper.selectConfirmedList(year, period, null);
        }
        return mapper.selectConfirmedList(year, period, currentEmpNo);
    }

    public CompetencyDTO.Info getDetail(Integer typeId, String empNo) {
        CompetencyDTO.Info info = mapper.selectCompetencyInfo(typeId, empNo);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();

        if (info != null) {
            // ADMIN은 모든 내용 열람 가능
            if ("ADMIN".equals(userDetails.getRole())) {
                info.setItems(mapper.selectCompetencyItems(typeId, empNo));
                return info;
            }

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

    @Transactional
    public void saveEvaluation(CompetencyDTO.SaveReq req) {
        if ("SELF".equals(req.getStep())) {
            for (CompetencyDTO.Item item : req.getItems()) {
                mapper.updateSelfAnswer(req.getEmpNo(), req.getTypeId(), item);
            }
            if ("Y".equals(req.getSubCheck())) {
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 1, 0);
            }
        } else if ("FIRST".equals(req.getStep())) {
            for (CompetencyDTO.Item item : req.getItems()) {
                mapper.updateFirstAnswer(req.getEmpNo(), req.getTypeId(), item);
            }
            if ("Y".equals(req.getSubCheck())) {
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 2, 0);
                mapper.updateEvalStatus(req.getTypeId(), req.getEmpNo(), 2, 1);
            }
        }
    }
}