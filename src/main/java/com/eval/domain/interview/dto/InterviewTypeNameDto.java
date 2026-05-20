package com.eval.domain.interview.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterviewTypeNameDto {
    private Long id;
    private String name;

    @Getter
    @Setter
    public static class InterviewCategoryLabels {
        private Long id;
        private List<String> categoryLabels;
    }
}