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
		            i.id, 
		            i.startAt AS startDateTime, 
		            i.endAt AS endDateTime, 
		            i.type AS interviewType,
		            i.evaluatorNo, 
		            e.name AS evaluatorName,
		            i.evaluateeNo, 
		            u.name AS evaluateeName,
		            i.subject, 
		            i.place, 
		            c.name AS status
		        )
		        FROM Interview i
		        LEFT JOIN Employee e ON i.evaluatorNo = e.empNo
		        LEFT JOIN Employee u ON i.evaluateeNo = u.empNo
		        LEFT JOIN CodeCommon c ON c.groupId = :groupId AND c.code = CAST(i.status AS string)
		        WHERE i.evaluatorNo = :empNo OR i.evaluateeNo = :empNo
		    """)
		    List<InterviewListDto> findInterviewList(@Param("empNo") String empNo, @Param("groupId") Long groupId);
}