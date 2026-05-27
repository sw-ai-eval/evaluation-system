package com.eval.domain.interview.service;

import com.eval.domain.codetable.CodeCommon;
import com.eval.domain.codetable.CodeCommonRepository;
import com.eval.domain.employee.EmployeeRepository;
import com.eval.domain.interview.Interview;
import com.eval.domain.interview.InterviewTopic;
import com.eval.domain.interview.InterviewTopicMapping;
import com.eval.domain.interview.dto.InterviewListDto;
import com.eval.domain.interview.dto.InterviewSaveRequestDto;
import com.eval.domain.interview.dto.InterviewTypeNameDto;
import com.eval.domain.interview.dto.InterviewTypeNameDto.InterviewCategoryLabels;
import com.eval.domain.interview.mapper.InterviewMapper;
import com.eval.domain.interview.repository.InterviewRepository;
import com.eval.domain.interview.repository.TopicMappingRepository;
import com.eval.domain.interview.repository.TopicRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewMapper interviewMapper;
    private final EmployeeRepository employeeRepository;
    private final TopicRepository topicRepository;
    private final TopicMappingRepository topicMappingRepository;
    private final CodeCommonRepository codeCommonRepository;

    public Page<InterviewListDto> getInterviewPage(
            String empNo, String position,
            String startDay, String employee,
            String type, String status, Pageable pageable, String role) {

        Map<String, Object> params = new HashMap<>();
        params.put("empNo", empNo);
        params.put("startDay", startDay);
        params.put("employee", employee);
        params.put("type", type);
        params.put("status", status);

        if (pageable != null) {
            // MSSQL용 페이징 파라미터
            params.put("limit", pageable.getPageSize());
            params.put("offset", pageable.getPageNumber() * pageable.getPageSize());
        }

        // 실제 조회
        List<InterviewListDto> list = interviewMapper.findInterviewList(params);

        // topics 매핑
        List<Long> ids = list.stream().map(InterviewListDto::getId).toList();
        Map<Long, List<String>> topicMap =
                topicMappingRepository.findByInterviewIds(ids)
                        .stream()
                        .collect(Collectors.groupingBy(
                                m -> m.getInterview().getId(),
                                Collectors.mapping(InterviewTopicMapping::getTopicName, Collectors.toList())
                        ));

        for (InterviewListDto dto : list) {
            dto.setTopics(topicMap.getOrDefault(dto.getId(), List.of()));
        }

        // 전체 갯수 조회 (페이징용)
        long total = interviewMapper.countInterviewList(params);

        // Page 객체 생성
        return new PageImpl<>(list, pageable, total);
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
    
    
    

    @Transactional
    public void saveInterview(InterviewSaveRequestDto dto, String evaluatorNo) {

        Interview interview;

        if (dto.getId() != null) {
            // 🔥 기존 인터뷰 수정
            Optional<Interview> optionalInterview = interviewRepository.findById(dto.getId());
            if (optionalInterview.isEmpty()) {
                throw new IllegalArgumentException("해당 면담이 존재하지 않습니다. id=" + dto.getId());
            }
            interview = optionalInterview.get();
        } else {
            // 🔥 새로운 면담 생성
            interview = new Interview();
            interview.setEvaluatorNo(evaluatorNo);
            interview.setCreatedAt(LocalDateTime.now());
            interview.setDeptId(employeeRepository.findDeptIdByEmpNo(evaluatorNo));
        }

        // 공통 필드 업데이트
        interview.setEvaluateeNo(dto.getEvaluatee());
        interview.setType(dto.getInterviewType());
        interview.setDetail(dto.getTextarea());
        interview.setStartAt(LocalDateTime.parse(dto.getStart()));
        interview.setEndAt(LocalDateTime.parse(dto.getEnd()));
        interview.setPlace(dto.getPlace());
        interview.setStatus(dto.getStatus());

        // 🔥 인터뷰 저장 (신규이든 수정이든)
        interviewRepository.save(interview);

        // 🔥 기존 토픽 삭제 후 새로 매핑
        topicMappingRepository.deleteByInterviewId(interview.getId());

        if (dto.getTopics() != null && !dto.getTopics().isEmpty()) {
            List<InterviewTopicMapping> mappings = dto.getTopics().stream()
                    .map(topicName -> {
                        InterviewTopicMapping m = new InterviewTopicMapping();
                        m.setInterview(interview);
                        m.setTopicName(topicName);
                        return m;
                    })
                    .toList();

            topicMappingRepository.saveAll(mappings);
        }
    }
    
    public void deleteInterview(InterviewSaveRequestDto request) {
    	interviewRepository.deleteById(request.getId());
    }

}