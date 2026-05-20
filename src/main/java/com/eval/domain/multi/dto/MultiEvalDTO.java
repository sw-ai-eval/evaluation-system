package com.eval.domain.multi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
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

        private List<String> labels = new ArrayList<>();

        private List<Double> myAvgScores = new ArrayList<>();
        private List<Double> allAvgScores = new ArrayList<>();
        
        private List<Integer> maxScores = new ArrayList<>();

    }
    
    @Getter 
    @Setter
    public static class CategoryAvgDto {
        private String categoryName;
        private Double avgScore;
    }
    
    @Getter 
    @Setter
    public static class MyCategoryScoreDto {

        private String categoryName;
        private Double totalScore;
        private int evaluatorCount;
 
    }
    
    // 다면 평가 결과 나온 후 화면에서 보여주는 용
    @Getter 
    @Setter
    public static class resultEvalDto {

        private String categoryName;
        private Long questionId;
        private Double score;
        private String content;
 
    }
    
    
    
    
}