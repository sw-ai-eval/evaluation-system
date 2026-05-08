package com.eval.domain.evaluation;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "dept_eval_weight_52")
@Getter @Setter @NoArgsConstructor
@IdClass(DeptEvalWeight.DeptEvalWeightId.class) 
public class DeptEvalWeight {

    @Id
    @Column(name = "dept_id")
    private String deptId;

    @Id
    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "weight")
    private Integer weight;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class DeptEvalWeightId implements Serializable {
        private String deptId;
        private Integer typeId;
    }
}