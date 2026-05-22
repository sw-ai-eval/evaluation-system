package com.eval.domain.competency.controller;

import com.eval.domain.competency.dto.CompetencyDTO;
import com.eval.domain.competency.service.CompetencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/evaluation/competency")
@RequiredArgsConstructor
public class CompetencyController {

    private final CompetencyService service;

    @GetMapping("/list")
    public String list(
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

        return "evaluation/competency/list";
    }

    @GetMapping("/detail/{typeId}/{empNo}")
    @ResponseBody
    public CompetencyDTO.Info getDetail(@PathVariable Integer typeId, @PathVariable String empNo) {
        return service.getDetail(typeId, empNo);
    }

    @PostMapping("/save")
    @ResponseBody
    public String save(@RequestBody CompetencyDTO.SaveReq request) {
        try {
            service.saveEvaluation(request);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
}