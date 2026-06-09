package com.eval.domain.home.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.eval.domain.home.dto.TodoListDto;
import com.eval.domain.home.dto.NoticeDetailDto;
import com.eval.domain.home.dto.NoticeListDto;

@Mapper
public interface HomeMapper {

	List<TodoListDto> selectInterviewTodoList(String empNo);
	
	List<TodoListDto> selectEvaluationTodoList(String empNo);
	
	List<NoticeListDto> selectNoticeList();
    
	NoticeDetailDto getNoticeDetail(Integer id);

	Long countIncompleteEvalByEmpNo(String empNo);
	
	Long countCompletedEvalByEmpNo(String empNo);

	long countAllEval();

	long countAllStartedEval();
	
	Long countAllNotStartedEvalEmpNum();

	List<TodoListDto> selectExecutiveEvaluationTodoList(String empNo);
	
}