package com.eval.domain.interview.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InterviewSaveRequestDto{

	private Long id;
    private String evaluatee;
    private Long interviewType; //엔티티에는 type
    private String start;
    private String end;
    private String place;
    private int status;
    private String textarea; // 엔티티에는 detail
    private List<String> topics = new ArrayList<>();


}