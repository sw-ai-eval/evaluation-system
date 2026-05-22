package com.eval.domain.performance.controller;

import com.eval.domain.performance.dto.PerformanceDTO;
import com.eval.domain.performance.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/evaluation/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService service;

    @GetMapping("/list")
    public String evalList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period,
            Model model) {

        List<Integer> yearList = service.getEvalYears();
        if (year == null && !yearList.isEmpty()) year = yearList.get(0);
        if (period == null) period = "연간";

        model.addAttribute("yearList", yearList);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("evalList", service.getEvalList(year, period));
        model.addAttribute("confirmedList", service.getConfirmedList(year, period));

        return "evaluation/performance/list";
    }

    @GetMapping("/detail/{typeId}/{empNo}")
    @ResponseBody
    public PerformanceDTO.Info getEvalDetail(@PathVariable Integer typeId, @PathVariable String empNo) {
        return service.getEvalDetail(typeId, empNo);
    }

    @PostMapping("/save")
    @ResponseBody
    public String saveEvaluation(@RequestBody PerformanceDTO.SaveReq request) {
        try {
            service.saveEvaluation(request);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
}