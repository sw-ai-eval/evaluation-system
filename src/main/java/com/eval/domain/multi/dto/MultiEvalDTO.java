package com.eval.domain.multi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultiEvalDTO {
	private int evalTypeId;
	private String evalTypeName;
	
	private Integer totalScore;
	
    private String evaluatorNo;
    private String evaluateeNo;
    private String evalName;
    private int step;
    private String statusName;
    
    // 평가 대상자 표시용
    private String evaluatorName;
    private String evaluateeName;
    private String position;
    private String deptId;
    private String deptName;
    
    private LocalDate startDate;
    private LocalDate endDate;

    private List<EvalItem> items;
    
    private List<CategorySummary> categorySummaries;
    
    private MultiChartDto chart;
    
    public String getEvalPeriod() {
        if (startDate == null && endDate == null) {
            return "";  // 둘 다 없으면 빈 문자열
        } else if (startDate == null) {
            return endDate.toString();
        } else if (endDate == null) {
            return startDate.toString();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return startDate.format(formatter) + " ~ " + endDate.format(formatter);
    }
    
    @Getter
    @Setter
    public static class EvalItem {
        private Long questionId;
        private String categoryName;
        private String explanation;
        private String question;
        private String questionType;
        private Integer weight;
        private Integer score;
        private String content;
        private Long mappingId;
        
    }
    
    @Getter
    @Setter
    public static class CategorySummary {
        private String categoryName;
        private int totalWeight;
        private String combinedGuide;
        private List<EvalItem> items;
    }
    
    @Getter
    @Setter
    public static class MultiChartDto {
        private String evaluateeNo;
        // 내 총점
        private BigDecimal myTotalScore;

        // 전체 부서장 평균 점수
        private BigDecimal allLeaderAvgScore;

        // 내 평균 점수
        private BigDecimal myAvgScore;
    }

    
}