package com.eval.domain.interview.service;

import com.eval.domain.interview.Interview;
import com.eval.domain.interview.dto.InterviewListDto;
import com.eval.domain.interview.repository.InterviewRepository;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;

    public List<InterviewListDto> getInterviewList(String empNo, String position) {
        return interviewRepository.findInterviewList(empNo);
    }


}