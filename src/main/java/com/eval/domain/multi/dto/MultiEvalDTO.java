package com.eval.domain.multi.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultiEvalDTO {
	private Long id;
    private String evaluatorNo;
    private String evaluateeNo;
    private String evalName;
    private int step;
    private String statusName;

    private Long typeId;
    
    // 평가 대상자 표시용
    private String evaluatorName;
    private String evaluateeName;
    private String position;
    private String deptId;
    private String deptName;
    
    private LocalDate startDate;
    private LocalDate endDate;

    private List<EvalItem> items;
    
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
        private String goalDesc;
        private int weight;
        private Integer selfScore;
        private String selfFeedback;
        private Integer firstScore;
        private String firstFeedback;
    }

    @Getter
    @Setter
    public static class MultiItem {
        private String categoryName;
        private String guide;
        private int weight;
    }
}