package com.eval.domain.home.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoListDto {
    String type; // 
    private String evaluatorName;
    private String evaluateeName;
    LocalDateTime dueDate;
    String status;
}