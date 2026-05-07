package com.eval.domain.evaluation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "eval_question_target_52")
@Getter @Setter @NoArgsConstructor
public class EvalItemTarget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_id")
    private Integer id;

    @Column(name = "question_id", nullable = false)
    private Integer questionId;

    @Column(name = "target_type", nullable = false, length = 10)
    private String targetType; // 'DEPT' (부서) 또는 'EMP' (사원)

    @Column(name = "target_value", nullable = false, length = 50)
    private String targetValue; // 부서ID 또는 사번
}