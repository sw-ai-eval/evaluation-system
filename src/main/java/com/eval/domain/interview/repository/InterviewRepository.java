package com.eval.domain.interview.repository;

import com.eval.domain.interview.Interview;
import com.eval.domain.interview.dto.InterviewListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    
}