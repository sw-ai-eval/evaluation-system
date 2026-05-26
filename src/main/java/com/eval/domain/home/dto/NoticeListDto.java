package com.eval.domain.home.dto;

import java.time.LocalDateTime;

import com.eval.domain.home.Notice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeListDto {
	
	private Integer id;
    private String type;
    private String title;
    private LocalDateTime createdAt;

    public static NoticeListDto from(Notice notice) {
        NoticeListDto dto = new NoticeListDto();
        dto.setId(notice.getNoticeId());
        dto.setType(notice.getType());
        dto.setTitle(notice.getTitle());
        dto.setCreatedAt(notice.getCreatedAt());
        return dto;
    }
}