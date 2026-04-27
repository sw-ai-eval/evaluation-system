package com.eval.domain.evaluation;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "dept_eval_weight_52")
@Getter @Setter @NoArgsConstructor
@IdClass(DeptEvalWeightId.class) // 복합키 설정
public class DeptEvalWeight {
    @Id
    @Column(name = "dept_id")
    private String deptId;

    @Id
    @Column(name = "type_id")
    private Integer typeId;

    private Integer weight;
}

@Data @NoArgsConstructor @AllArgsConstructor
class DeptEvalWeightId implements Serializable {
    private String deptId;
    private Integer typeId;
}