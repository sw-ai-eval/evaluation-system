package com.eval.domain.interview.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class InterviewDetailDto {
	private final Long id;
    private final LocalDateTime startDateTime; // 시작 시간
    private final LocalDateTime endDateTime;
    private final Long interviewType;

    private final String evaluateeNo;
    private final String evaluateeName;

    private List<String> topics; 
    private final String place;
    
    private final int status;
    private final String detail;

    public InterviewDetailDto(
    		Long id,
    		LocalDateTime startDateTime,
    		LocalDateTime endDateTime,
            Long interviewType,
            String evaluateeNo,
            String evaluateeName,
            List<String> topics, 
            String place,
            int status,
            String detail
    ) {
    	this.id=id;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.interviewType = interviewType;
        this.evaluateeNo = evaluateeNo;
        this.evaluateeName = evaluateeName;
        this.topics = topics;
        this.place = place;
        this.status = status;
        this.detail = detail;
        
        
    }
    public String getInterviewDateRange() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return formatter.format(startDateTime) + " ~ " + formatter.format(endDateTime);
    }
}