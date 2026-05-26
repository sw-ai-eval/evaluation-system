package com.eval.domain.finalgrade.controller;

import com.eval.domain.dept.dto.DepartmentDto;
import com.eval.domain.dept.mapper.DepartmentMapper;
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
    private final DepartmentMapper departmentMapper;

    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String empNo,
            @RequestParam(required = false) String deptId,
            Model model) {

        List<Integer> yearList = service.getYears();
        if (year == null && !yearList.isEmpty()) year = yearList.get(0);
        if (period == null) period = "연간";

        model.addAttribute("yearList", yearList);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("selectedEmpNo", empNo);
        model.addAttribute("selectedDeptId", deptId);
        model.addAttribute("staffList", service.getStaffList(year, period, empNo, deptId));
        model.addAttribute("leaderList", service.getLeaderList(year, period, empNo, deptId));
        model.addAttribute("deptStats", service.getDeptStat(year));
        model.addAttribute("deptList", departmentMapper.selectDepartmentList());

        return "evaluation/finalgrade/list";
    }

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