package com.eval.domain.multi.dto;

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
    private String status;
    private List<EvalItem> items;

    @Getter
    @Setter
    public static class EvalItem {   // static inner class
        private Long questionId;
        private String categoryName;
        private String goalDesc;
        private int weight;
        private Integer selfScore;
        private String selfFeedback;
        private Integer firstScore;
        private String firstFeedback;
    }
}