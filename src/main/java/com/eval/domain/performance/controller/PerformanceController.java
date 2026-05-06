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

    // 1. 화면 띄우기 + 검색 리스트 데이터
    @GetMapping("/list")
    public String evalList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period,
            Model model) {
        
        // 기준년도 리스트 가져오기
        List<Integer> yearList = service.getEvalYears();
        model.addAttribute("yearList", yearList);
        
        // 기본값 설정: 넘어온 year가 없으면 최신 년도로 세팅
        if (year == null && !yearList.isEmpty()) {
            year = yearList.get(0); 
        }
        
        if (period == null) {
            period = "연간";
        }
        
        // 검색 조건 유지용 (화면 select box에서 th:selected로 사용)
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedPeriod", period);
    	
        // year와 period를 모두 서비스로 전달하여 필터링된 목록 조회
        List<PerformanceDTO.Info> evalList = service.getEvalList(year, period);
        model.addAttribute("evalList", evalList);
        
        return "evaluation/performance/list";
    }

    // 2. 우측 상세 내역 가져오기 (Ajax 통신용)
    @GetMapping("/detail/{typeId}/{empNo}")
    @ResponseBody
    public PerformanceDTO.Info getEvalDetail(@PathVariable Integer typeId, @PathVariable String empNo) {
        return service.getEvalDetail(typeId, empNo);
    }

    // 3. 본인/1차 평가 저장 및 제출
    @PostMapping("/save")
    @ResponseBody
    public String saveEvaluation(@RequestBody PerformanceDTO.SaveReq request) {
        try {
            service.saveEvaluation(request);
            return "success";
        } catch (Exception e) {
            e.printStackTrace(); // 에러 로그 확인용
            return "error: " + e.getMessage();
        }
    }
}