package com.eval.domain.interview.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class InterviewListDto {

    private final Long id;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final Long interviewType;
    private final String interviewTypeName;

    private final String evaluatorNo;
    private final String evaluatorName;

    private final String evaluateeNo;
    private final String evaluateeName;

    private List<String> topics;  // ✅ 변경 가능
    private final String place;

    private String status;   // ✅ 변경 가능

    public InterviewListDto(
            Long id,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Long interviewType,
            String interviewTypeName,
            String evaluatorNo,
            String evaluatorName,
            String evaluateeNo,
            String evaluateeName,
            String place,
            String status
    ) {
        this.id = id;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.interviewType=interviewType;
        this.interviewTypeName = interviewTypeName;
        this.evaluatorNo = evaluatorNo;
        this.evaluatorName = evaluatorName;
        this.evaluateeNo = evaluateeNo;
        this.evaluateeName = evaluateeName;
        this.place = place;
        this.status = status;
    }

    public String getInterviewDateRange() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return formatter.format(startDateTime) + " ~ " + formatter.format(endDateTime);
    }
}