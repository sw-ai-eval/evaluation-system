package com.eval.domain.home.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeDetailDto {

    private Integer id;

    private String createBy;   // 직원 이름
    private String updateBy;   // 직원 이름

    private String type;
    private String title;
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}