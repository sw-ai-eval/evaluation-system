package com.eval.domain.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eval.domain.interview.InterviewTopic;

public interface TopicRepository extends JpaRepository<InterviewTopic, Long> {
	InterviewTopic findByName(String name);
}