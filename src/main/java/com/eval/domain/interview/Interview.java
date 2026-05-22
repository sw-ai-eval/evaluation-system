package com.eval.domain.interview;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "interview_52")
@Getter
@Setter
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluator_no", nullable = false, length = 50)
    private String evaluatorNo;

    @Column(name = "evaluatee_no", nullable = false, length = 50)
    private String evaluateeNo;

    @Column(name = "type", nullable = false, length = 50)
    private Long type;

    @Column(name = "detail", nullable = false, columnDefinition = "nvarchar(max)")
    private String detail;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "place", length = 100)
    private String place;

    @Column(name = "dept_id", length = 50)
    private String deptId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "status", nullable = false)
    private int status;
    
    @OneToMany(mappedBy = "interview")
    private List<InterviewTopicMapping> topics;


}