package com.eval.domain.home.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.eval.domain.home.Notice;
import com.eval.domain.home.NoticeRepository;
import com.eval.domain.home.dto.NoticeDetailDto;
import com.eval.domain.home.dto.NoticeDto;
import com.eval.domain.home.dto.TodoListDto;
import com.eval.domain.home.dto.NoticeListDto;
import com.eval.domain.home.mapper.HomeMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeService {
	private final HomeMapper homeMapper;
	private final NoticeRepository noticeRepository;
	
	public List<TodoListDto> getTodoList(String empNo, String position){
		

	    // 1. 면담 할 일
	    List<TodoListDto> interviewList = homeMapper.selectInterviewTodoList(empNo);
	    List<TodoListDto> evalList =null;
	    // 2. 평가 할 일
	    if("임원".equals(position)) {
	    	evalList=homeMapper.selectExecutiveEvaluationTodoList(empNo);
	    }else {
	    	evalList = homeMapper.selectEvaluationTodoList(empNo);
	    }
	    

	    List<TodoListDto> result = new ArrayList<>();
	    result.addAll(interviewList);
	    result.addAll(evalList);

	    result.sort(
	    	    Comparator.comparing(
	    	        TodoListDto::getDueDate,
	    	        Comparator.nullsLast(Comparator.naturalOrder())
	    	    )
	    	);

	    return result;
	}

	public void saveNotice(NoticeDto dto) {

	    Notice notice = new Notice();

	    notice.setCreateBy(dto.getCreateBy());
	    notice.setType(dto.getType());
	    notice.setTitle(dto.getTitle());
	    notice.setContent(dto.getContent());
	    notice.setCreatedAt(LocalDateTime.now());

	    noticeRepository.save(notice);
	}
	
	
	public Page<NoticeListDto> getNoticePage(String title, int page, int size) {

	    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

	    Page<Notice> result;

	    if (title == null || title.isBlank()) {
	        result = noticeRepository.findAll(pageable);
	    } else {
	        result = noticeRepository.findByTitleContaining(title, pageable);
	    }

	    return result.map(NoticeListDto::from);
	}

	public NoticeDetailDto getNoticeDetail(Integer id) {
		return homeMapper.getNoticeDetail(id);
	}

	@Transactional
	public void deleteNotice(Integer id) {
		noticeRepository.deleteById(id);

	}

	@Transactional
	public void updateNotice(NoticeDto dto, String empNo) {

	    Notice notice = noticeRepository.findById(dto.getId())
	            .orElseThrow(() -> new IllegalArgumentException("공지 없음"));

	    notice.setTitle(dto.getTitle());
	    notice.setContent(dto.getContent());
	    notice.setType(dto.getType());

	    notice.setUpdateBy(empNo); // 또는 로그인 사용자
	    notice.setUpdateAt(LocalDateTime.now());
	}
	
	public double getMyOngoingEvalPercent(String empNo) {

	    long ongoing = homeMapper.countIncompleteEvalByEmpNo(empNo);
	    long total = homeMapper.countCompletedEvalByEmpNo(empNo);

	    if (total == 0) {
	        return 0.0;
	    }

	    double percent = ((double)(total - ongoing) / total) * 100;

	    return Math.round(percent * 100) / 100.0;
	}
	
	public double getDeptOngoingEvalPercent()
	{
		long Complete = homeMapper.countAllStartedEval();
	    long total = homeMapper.countAllEval();
	    
	    if (total == 0) {
	        return 0.0;
	    }

	    double percent = ((double)Complete/ total) * 100;

	    return Math.round(percent * 100) / 100.0;
	}

}