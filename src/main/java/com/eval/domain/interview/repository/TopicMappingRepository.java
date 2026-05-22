package com.eval.domain.interview.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eval.domain.interview.Interview;
import com.eval.domain.interview.InterviewTopicMapping;



public interface TopicMappingRepository extends JpaRepository<InterviewTopicMapping, Long> {
	@Query("""
			SELECT m
			FROM InterviewTopicMapping m
			WHERE m.interview.id IN :ids
			""")
			List<InterviewTopicMapping> findByInterviewIds(@Param("ids") List<Long> ids);
	
	@Query("""
			select i
			from Interview i
			left join fetch i.topics
			where i.id = :id
			""")
			Interview findByIdWithTopics(@Param("id") Long id);
	
	@Query("""
			select i
			from Interview i
			left join fetch i.topics
			""")
			List<Interview> findAllWithTopics();

	void deleteByInterviewId(Long id);
}