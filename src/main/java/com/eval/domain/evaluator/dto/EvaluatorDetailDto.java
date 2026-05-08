package com.eval.domain.evaluator.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluatorDetailDto {

    private Long evalId;   // ⭐ 여기만 존재

    private String deptId;
    private String deptName;

    private String empNo;
    private String empName;
    private String position;

    private List<EvaluatorDto> firstEvaluators;
    private String finalEvaluator;
    private String finalEvaluatorName;
    
    private List<AvailableEmployeeDto> availableEmployees;
}