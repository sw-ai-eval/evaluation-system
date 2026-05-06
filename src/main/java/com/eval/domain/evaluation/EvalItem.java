package com.eval.domain.evaluation;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eval_question_52")
@Getter @Setter @NoArgsConstructor
public class EvalItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private EvalType evalType;

    private String category;
    
    @Column(name = "question")
    private String content;

    private String explanation;
    
    @Column(name = "weight", nullable = false, columnDefinition = "int default 0")
    private int weight;

    @Column(name = "question_type") 
    private String answerType;

    @Column(name = "is_common") 
    private boolean isCommon;
    
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}