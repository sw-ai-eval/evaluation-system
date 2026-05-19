package com.eval.domain.finalgrade.service;

import com.eval.domain.finalgrade.dto.FinalGradeDTO;
import com.eval.domain.finalgrade.mapper.FinalGradeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinalGradeService {

    private final FinalGradeMapper mapper;

    public List<Integer> getYears() {
        return mapper.selectYears();
    }

    public List<FinalGradeDTO.StaffInfo> getStaffList(Integer year, String period, String empNo) {
        return mapper.selectStaffList(year, period, empNo);
    }

    public List<FinalGradeDTO.LeaderInfo> getLeaderList(Integer year, String period, String empNo) {
        return mapper.selectLeaderList(year, period, empNo);
    }

    public List<FinalGradeDTO.DeptStat> getDeptStat(Integer year) {
        return mapper.selectDeptStat(year);
    }

    @Transactional
    public void adjustGrade(FinalGradeDTO.GradeAdjustReq req) {
        String updatedBy = getCurrentEmpNo();
        mapper.upsertFinalResult(
                req.getEmpNo(),
                req.getYear(),
                req.getConfirmedGrade(),
                gradeToScore(req.getConfirmedGrade()),
                0,
                req.getReason(),
                "{\"perf\":60,\"comp\":40}",
                updatedBy
        );
    }

    @Transactional
    public void confirmGrades(List<String> empNos, Integer year) {
        String updatedBy = getCurrentEmpNo();
        for (String empNo : empNos) {
            mapper.confirmFinalResult(empNo, year, updatedBy);
            mapper.updateMappingStatus(empNo, year);
        }
    }

    private String getCurrentEmpNo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    private int gradeToScore(String grade) {
        if (grade == null) return 0;
        return switch (grade) {
            case "S" -> 100;
            case "A" -> 90;
            case "B" -> 80;
            case "C" -> 70;
            case "D" -> 60;
            default  -> 0;
        };
    }
}