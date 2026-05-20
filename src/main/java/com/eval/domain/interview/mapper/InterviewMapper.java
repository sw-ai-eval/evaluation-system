package com.eval.domain.interview.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import com.eval.domain.interview.dto.InterviewTypeNameDto;

@Mapper
public interface InterviewMapper {
	
    List<String> getCategoryLabelsForType(Long typeId);
    
    List<InterviewTypeNameDto> findAllInterviewTypeList();
}