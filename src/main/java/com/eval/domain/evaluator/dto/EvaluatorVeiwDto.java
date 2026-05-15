package com.eval.domain.evaluator.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EvaluatorVeiwDto {

    private String deptId;
    private String deptName;

    private String empNo;
    private String empName;
    private String position;

    private List<EvaluatorDto> firstEvaluators;
    
    private String finalEvaluatorName;
    private String finalEvaluatorEmpNo;

    private String systemType;
    private String firstEvaluatorNames;
    
    private int status;


    // ✅ 생성자 반드시 클래스 내부
    public EvaluatorVeiwDto(
            String deptId,
            String deptName,
            String empNo,
            String empName,
            String position,
            int step,
            String evaluatorEmpNo,
            String evaluatorName,
            String systemType,
            int status
    ) {
        
        this.deptId = deptId;
        this.deptName = deptName;
        this.empNo = empNo;
        this.empName = empName;
        this.position = position;
        this.systemType=systemType;
        this.status=status;
        this.firstEvaluators = new ArrayList<>();
        
        if (step == 1) {
        	this.firstEvaluators.add(
        		    new EvaluatorDto(
        		        evaluatorEmpNo,
        		        evaluatorName
        		    )
        		);
        }

        if (step == 2) {
            this.finalEvaluatorEmpNo = evaluatorEmpNo;
            this.finalEvaluatorName = evaluatorName;
        }
    }
}