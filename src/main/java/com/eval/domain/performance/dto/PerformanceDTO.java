package com.eval.domain.performance.dto;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PerformanceDTO {

    @Data
    public static class Info {
        private Integer typeId;
        private String evalPeriod; 
        private String deptName;
        private String leaderEmpNo;
        private String position;
        private String empNo;
        private String empName; 
        private String status;
        private String totalGrade;
        
        private List<Item> items;
    }

    @Data
    public static class Item {
        private Integer questionId;
        private String categoryName;
        private String goalDesc;
        private Integer weight;
        
        private String selfFeedback;
        private Integer selfScore;
        private String firstFeedback;
        private Integer firstScore;
    }

    @Data
    public static class SaveReq {
        private String step;
        private String subCheck;
        private Integer typeId;
        private String empNo;
        private List<Item> items;
    }
    
    @Data
    public static class EvalType {
        private Integer id;
        private String name;
    }
}