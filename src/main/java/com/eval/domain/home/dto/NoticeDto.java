package com.eval.domain.home.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeDto{
	private Integer id;
	private String createBy;
	private String type;
	private String title; 
    private String content;
    private LocalDateTime createdAt;


}