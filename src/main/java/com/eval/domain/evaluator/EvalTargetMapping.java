package com.eval.domain.evaluator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.eval.domain.evaluation.EvalType;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "eval_target_mapping_52")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvalTargetMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String evaluatorNo;
    private String evaluateeNo;
    private int step;
    private String systemType;
    private String updatedBy;
    private LocalDateTime updatedAt;
    
    private String deptId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eval_type_id")
    private EvalType typeId;
}
