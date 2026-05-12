package com.eval.domain.competency.dto;

import lombok.Data;
import java.util.List;

public class CompetencyDTO {

    // 1. 목록 및 상세 정보용 DTO
    @Data
    public static class Info {
        private Integer typeId;
        private String empNo;
        private String empName;
        private String deptName;
        private String position;
        private String status;        // 본인평가중, 1차평가대기 등
        private String leaderEmpNo;   // 1차 평가자(부서장) 사번
        private String totalGrade;    // 종합 등급
        private List<Item> items;     // 평가 문항 리스트
        private String evalPeriod;
    }

    // 2. 개별 평가 문항 (등급 방식)
    @Data
    public static class Item {
        private Integer questionId;
        private String categoryName;  // 핵심공통역량, 직무역량 등
        private String itemName;      // 역량 항목명
        private String goalDesc;      // 평가착안점
        private Integer weight;       // 배점

        private String selfGrade;     // 본인 평가 등급 (S, A, B, C, D)
        private String selfFeedback;  // 본인 평가 의견
        
        private String firstGrade;    // 1차 평가 등급
        private String firstFeedback; // 1차 평가 의견
    }

    // 3. 저장 요청용 DTO
    @Data
    public static class SaveReq {
        private String step;          // "SELF" 또는 "FIRST"
        private String subCheck;      // 제출 여부 ("Y" 또는 "N")
        private Integer typeId;
        private String empNo;
        private List<Item> items;
    }
}