package com.eval.domain.interview.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InterviewListDto {

    private final LocalDateTime interviewDate;
    private final String interviewType;

    private final String evaluatorNo;
    private final String evaluatorName;

    private final String evaluateeNo;
    private final String evaluateeName;

    private final String subject;
    private final String place;

    public InterviewListDto(
            LocalDateTime interviewDate,
            String interviewType,
            String evaluatorNo,
            String evaluatorName,
            String evaluateeNo,
            String evaluateeName,
            String subject,
            String place
    ) {
        this.interviewDate = interviewDate;
        this.interviewType = interviewType;
        this.evaluatorNo = evaluatorNo;
        this.evaluatorName = evaluatorName;
        this.evaluateeNo = evaluateeNo;
        this.evaluateeName = evaluateeName;
        this.subject = subject;
        this.place = place;
    }
}