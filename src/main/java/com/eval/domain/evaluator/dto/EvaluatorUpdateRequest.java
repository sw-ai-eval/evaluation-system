package com.eval.domain.evaluator.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluatorUpdateRequest {

    private String deptId;
    private String empNo;
    private List<String> firstEvaluators;
    private String finalEvaluator;

}