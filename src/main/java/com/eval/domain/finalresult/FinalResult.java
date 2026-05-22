package com.eval.domain.finalresult;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.eval.domain.employee.Employee;


@Setter
@Getter
@Entity
@Table(name = "final_result_52")
public class FinalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "emp_no", nullable = false)
    private Employee employee; 

    @Column(length = 1)
    private String grade;

    private Integer finalScore;

    @Column(nullable = false)
    private Boolean status; 

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "weight_ratio", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String weightRatio; 

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
    
    public FinalResult() {}
    
    @Builder
    public FinalResult(Employee employee, Integer year, String grade, Integer finalScore, Boolean status,
                       String weightRatio, LocalDateTime createdAt, String updatedBy) {
        this.employee = employee;
        this.year = year;
        this.grade = grade;
        this.finalScore = finalScore;
        this.status = status;
        this.weightRatio = weightRatio;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
    }

}