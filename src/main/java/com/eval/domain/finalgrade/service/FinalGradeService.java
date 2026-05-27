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

    /**
     * 등급 조정 저장 + 부서 내 자동 재조정
     * 반환값: 재조정된 사원 목록 (알림용)
     */
    @Transactional
    public List<String> adjustGrade(FinalGradeDTO.GradeAdjustReq req) {
        String updatedBy = getCurrentEmpNo();

        // 1. 먼저 해당 사원 등급 저장
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

        // 2. 해당 사원의 부서/직위 조회
        String deptId = req.getDeptId();
        if (deptId == null || deptId.isEmpty()) return new ArrayList<>();

        // 3. 부서장이면 LEADER 정책, 부서원이면 부서별 정책
        boolean isLeader = "부서장".equals(req.getPosition());
        String policyKey = isLeader ? "LEADER" : deptId;
        DeptEvalGrade policy = gradeRepository.findById(policyKey).orElse(null);
        if (policy == null) return new ArrayList<>();

        // 4. 부서장이면 전체 부서장 목록, 부서원이면 해당 부서 사원 목록
        List<FinalGradeDTO.StaffScore> staffScores = isLeader
                ? mapper.selectLeaderScoreList(req.getYear())
                : mapper.selectStaffScoreByDept(deptId, req.getYear());
        if (staffScores.isEmpty()) return new ArrayList<>();

        // 5. 누적 비율 기준으로 등급 자동 배분
        // grade_s=10, grade_a=30, grade_b=70, grade_c=90, grade_d=100 (누적)
        int total = staffScores.size();
        List<String> changed = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            FinalGradeDTO.StaffScore staff = staffScores.get(i);

            // 이미 확정된 사원은 건드리지 않음
            if (staff.getConfirmStatus() != null && staff.getConfirmStatus() == 1) continue;

            // 현재 순위 퍼센트 (1-based)
            double pct = (double)(i + 1) / total * 100;

            String newGrade;
            if (pct <= policy.getGradeS()) newGrade = "S";
            else if (pct <= policy.getGradeA()) newGrade = "A";
            else if (pct <= policy.getGradeB()) newGrade = "B";
            else if (pct <= policy.getGradeC()) newGrade = "C";
            else newGrade = "D";

            // 등급이 바뀐 경우만 업데이트 + 알림
            String prevGrade = staff.getCurrentGrade();
            if (prevGrade != null && !prevGrade.equals(newGrade)
                    && !staff.getEmpNo().equals(req.getEmpNo())) {
                changed.add(staff.getEmpNo() + " (" + prevGrade + " → " + newGrade + ")");
            }

            // 저장 (임원이 직접 변경한 사원 제외)
            if (!staff.getEmpNo().equals(req.getEmpNo())) {
                mapper.upsertFinalResult(
                        staff.getEmpNo(),
                        req.getYear(),
                        newGrade,
                        null,
                        0,
                        "등급 자동 재조정",
                        "{}",
                        updatedBy
                );
            }
        }

        return changed;
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
}