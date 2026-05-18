package com.eval.domain.interview.repository;

import com.eval.domain.interview.Interview;
import com.eval.domain.interview.dto.InterviewListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("""
    SELECT new com.eval.domain.interview.dto.InterviewListDto(
        i.startAt,
        i.type AS interviewType,
        i.evaluatorNo,
        e.name,
        i.evaluateeNo,
        u.name,
        i.subject,
        i.place
    )
    FROM Interview i
    LEFT JOIN Employee e ON i.evaluatorNo = e.empNo
    LEFT JOIN Employee u ON i.evaluateeNo = u.empNo
    WHERE i.evaluatorNo = :empNo
       OR i.evaluateeNo = :empNo
    """)
    List<InterviewListDto> findInterviewList(@Param("empNo") String empNo);
}