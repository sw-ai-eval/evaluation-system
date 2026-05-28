package com.eval.domain.interview.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.eval.domain.interview.dto.InterviewListDto;
import com.eval.domain.interview.dto.InterviewTypeNameDto;

@Mapper
public interface InterviewMapper {
	
    List<String> getCategoryLabelsForType(Long typeId);
    
    List<InterviewTypeNameDto> findAllInterviewTypeList();

	List<InterviewListDto> findInterviewList(Map<String, Object> params);

	long countInterviewList(Map<String, Object> params);

	long countOngoingInterviewsByEmpNo(String empNo);

	List<InterviewListDto> findAllInterviewList(Map<String, Object> params);

	long countAllInterviewList(Map<String, Object> params);
}