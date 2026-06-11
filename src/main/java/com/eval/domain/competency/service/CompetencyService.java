package com.eval.domain.competency.service;

import com.eval.domain.competency.dto.CompetencyDTO;
import com.eval.domain.competency.mapper.CompetencyMapper;
import com.eval.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        if ("ADMIN".equals(userDetails.getRole())) {
            return mapper.selectEvalList(year, period, null, null);
        }
        if (userDetails.isExecutive()) {
            return mapper.selectEvalList(year, period, null, userDetails.getDeptCode());
        }
        return mapper.selectEvalList(year, period, currentEmpNo, null);
    }

    public List<CompetencyDTO.Info> getConfirmedList(Integer year, String period) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();

        if ("ADMIN".equals(userDetails.getRole())) {
            return mapper.selectConfirmedList(year, period, null, null);
        }
        if (userDetails.isExecutive()) {
            return mapper.selectConfirmedList(year, period, null, userDetails.getDeptCode());
        }
        return mapper.selectConfirmedList(year, period, currentEmpNo, null);
    }

    public CompetencyDTO.Info getDetail(Integer typeId, String empNo) {
        CompetencyDTO.Info info = mapper.selectCompetencyInfo(typeId, empNo);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();

        if (info != null) {
            // ADMIN, 임원은 모든 내용 열람 가능
            if ("ADMIN".equals(userDetails.getRole()) || userDetails.isExecutive()) {
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
        // 평가 기간 체크
        Map<String, Object> period = mapper.selectEvalPeriod(req.getTypeId());
        if (period != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = period.get("startDate") != null
                    ? ((Timestamp) period.get("startDate")).toLocalDateTime() : null;
            LocalDateTime endDate   = period.get("endDate") != null
                    ? ((Timestamp) period.get("endDate")).toLocalDateTime() : null;
            if (startDate != null && now.isBefore(startDate)) {
                throw new IllegalStateException("평가 기간이 시작되지 않았습니다.");
            }
            if (endDate != null && now.isAfter(endDate)) {
                throw new IllegalStateException("평가 기간이 종료되었습니다.");
            }
        }
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