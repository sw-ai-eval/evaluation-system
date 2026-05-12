package com.eval.domain.competency.controller;

import com.eval.domain.competency.dto.CompetencyDTO;
import com.eval.domain.competency.service.CompetencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/evaluation/competency")
@RequiredArgsConstructor
public class CompetencyController {

    private final CompetencyService service;

    // 1. 화면 띄우기 + 검색 리스트 데이터 (성과평가 예시 로직 적용)
    @GetMapping("/list")
    public String competencyList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period,
            Model model) {
        
        // 기준년도 리스트 가져오기 (Service에서 DB 조회)
        List<Integer> yearList = service.getEvalYears();
        model.addAttribute("yearList", yearList);
        
        // 기본값 설정: 넘어온 year가 없으면 최신 년도로 세팅
        if (year == null && !yearList.isEmpty()) {
            year = yearList.get(0); 
        }
        
        if (period == null) {
            period = "연간";
        }
        
        // 검색 조건 유지용 (HTML select box에서 사용)
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedPeriod", period);
        
        // 필터링된 역량평가 목록 조회
        List<CompetencyDTO.Info> evalList = service.getEvalList(year, period);
        model.addAttribute("evalList", evalList);
        
        return "evaluation/competency/list";
    }

    // 2. 우측 상세 내역 가져오기 (Ajax 통신용)
    @GetMapping("/detail/{typeId}/{empNo}")
    @ResponseBody
    public CompetencyDTO.Info getEvalDetail(@PathVariable Integer typeId, @PathVariable String empNo) {
        return service.getDetail(typeId, empNo);
    }

    // 3. 본인/1차 평가 저장 및 제출
    @PostMapping("/save")
    @ResponseBody
    public String saveEvaluation(@RequestBody CompetencyDTO.SaveReq request) {
        try {
            service.saveEvaluation(request);
            return "success";
        } catch (Exception e) {
            e.printStackTrace(); 
            return "error: " + e.getMessage();
        }
    }
}