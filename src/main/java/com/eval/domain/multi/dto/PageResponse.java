package com.eval.domain.multi.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageResponse<T> {
    private List<T> content;
    private int totalCount;
    private int totalPages;
    private int page;
    private int size;

    public PageResponse(List<T> content, int totalCount, int page, int size) {
        this.content = content;
        this.totalCount = totalCount;
        this.size = size;
        this.page = page;
        this.totalPages = (int) Math.ceil((double) totalCount / size);
    }


}