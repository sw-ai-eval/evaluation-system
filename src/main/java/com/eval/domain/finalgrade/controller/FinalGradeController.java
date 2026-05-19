package com.eval.domain.finalgrade.controller;

import com.eval.domain.finalgrade.dto.FinalGradeDTO;
import com.eval.domain.finalgrade.service.FinalGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/evaluation/final-grade")
@RequiredArgsConstructor
public class FinalGradeController {

    private final FinalGradeService service;

    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period,
            Model model) {

        List<Integer> yearList = service.getYears();
        if (year == null && !yearList.isEmpty()) year = yearList.get(0);
        if (period == null) period = "연간";

        model.addAttribute("yearList", yearList);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("staffList", service.getStaffList(year, period, null));
        model.addAttribute("leaderList", service.getLeaderList(year, period, null));
        model.addAttribute("deptStats", service.getDeptStat(year));

        return "evaluation/finalgrade/list";
    }

    /** 사원 탭 Ajax 검색 */
    @GetMapping("/search/staff")
    @ResponseBody
    public List<FinalGradeDTO.StaffInfo> searchStaff(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String empNo) {
        return service.getStaffList(year, period, empNo);
    }

    /** 부서장 탭 Ajax 검색 */
    @GetMapping("/search/leader")
    @ResponseBody
    public List<FinalGradeDTO.LeaderInfo> searchLeader(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String empNo) {
        return service.getLeaderList(year, period, empNo);
    }

    /** 등급 조정 저장 */
    @PostMapping("/adjust")
    @ResponseBody
    public String adjust(@RequestBody FinalGradeDTO.GradeAdjustReq req) {
        try {
            service.adjustGrade(req);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    /** 최종 확정 */
    @PostMapping("/confirm")
    @ResponseBody
    public String confirm(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<String> empNos = (List<String>) body.get("empNos");
            Integer year = (Integer) body.get("year");
            service.confirmGrades(empNos, year);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
}