package com.eval.domain.empscore.controller;

import com.eval.domain.empscore.service.EmpScoreService;
import com.eval.domain.dept.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/evaluation/employee-score")
@RequiredArgsConstructor
public class EmpScoreController {

    private final EmpScoreService service;
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
        model.addAttribute("deptList", departmentMapper.selectDepartmentList());

        return "evaluation/empscore/list";
    }
}