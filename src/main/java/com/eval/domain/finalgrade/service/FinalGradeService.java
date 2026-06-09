package com.eval.domain.finalgrade.service;

import com.eval.domain.evaluation.DeptEvalGrade;
import com.eval.domain.evaluation.repository.DeptEvalGradeRepository;
import com.eval.domain.finalgrade.dto.FinalGradeDTO;
import com.eval.domain.finalgrade.mapper.FinalGradeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinalGradeService {

    private final FinalGradeMapper mapper;
    private final DeptEvalGradeRepository gradeRepository;

    public List<Integer> getYears() {
        return mapper.selectYears();
    }

    public List<FinalGradeDTO.StaffInfo> getStaffList(Integer year, String period, String empNo, String deptId) {
        return mapper.selectStaffList(year, period, empNo, deptId);
    }

    public List<FinalGradeDTO.LeaderInfo> getLeaderList(Integer year, String period, String empNo, String deptId) {
        return mapper.selectLeaderList(year, period, empNo, deptId);
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
                null,
                0,
                req.getReason(),
                "{}",
                updatedBy
        );
    }


    @Transactional
    public void confirmGrades(List<String> empNos, Integer year) {
        String updatedBy = getCurrentEmpNo();
        for (String empNo : empNos) {
            mapper.confirmFinalResult(empNo, year, updatedBy);
            mapper.updateMappingStatus(empNo, year);
            mapper.updateExecutiveMappingStatus(empNo, year,updatedBy);
        }
    }

    private String getCurrentEmpNo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}