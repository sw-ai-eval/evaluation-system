package com.eval.domain.evaluator.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailableEmployeeDto {
    private String empNo;
    private String name;

    public AvailableEmployeeDto(String empNo, String name) {
        this.empNo = empNo;
        this.name = name;
    }
}