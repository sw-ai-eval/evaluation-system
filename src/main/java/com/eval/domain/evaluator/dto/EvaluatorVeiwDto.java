package com.eval.domain.evaluator.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluatorVeiwDto {

    private String deptId;
    private String deptName;
    
    private String empNo;
    private String empName;
    private String position;

    private String firstEvaluators;   // "홍길동, 김철수"
    private String finalEvaluator;  // "이임원"
    
    private String updateBy;
    private LocalDateTime updatedAt;
}