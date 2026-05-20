package com.eval.domain.interview.service;

import com.eval.domain.interview.Interview;
import com.eval.domain.interview.dto.InterviewListDto;
import com.eval.domain.interview.dto.InterviewTypeNameDto;
import com.eval.domain.interview.dto.InterviewTypeNameDto.InterviewCategoryLabels;
import com.eval.domain.interview.mapper.InterviewMapper;
import com.eval.domain.interview.repository.InterviewRepository;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewMapper interviewMapper;

    public List<InterviewListDto> getInterviewList(String empNo, String position) {
        return interviewRepository.findInterviewList(empNo,4L);
    }
    
    public InterviewCategoryLabels getLabelsForType(Long typeId){
        List<String> categories = interviewMapper.getCategoryLabelsForType(typeId);

        InterviewCategoryLabels dto = new InterviewCategoryLabels();
        dto.setId(typeId);
        dto.setCategoryLabels(categories);

        return dto;
    }

    public List<InterviewTypeNameDto> getAllInterviewTypes() {
        return interviewMapper.findAllInterviewTypeList();
    }


}