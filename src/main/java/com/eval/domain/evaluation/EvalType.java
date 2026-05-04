package com.eval.domain.evaluation;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eval_type_52")
@Getter @Setter @NoArgsConstructor
public class EvalType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean status;
    private Integer year;

    @Column(name = "guideline")
    private String guideline;
    
    @Column(name = "has_weight")
    private boolean hasWeight = true; 

    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}