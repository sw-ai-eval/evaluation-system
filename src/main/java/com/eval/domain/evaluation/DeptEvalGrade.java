package com.eval.domain.evaluation;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dept_eval_grade_52")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DeptEvalGrade {
    
    @Id
    @Column(name = "dept_id")
    private String deptId;

    @Column(name = "grade_s")
    private Integer gradeS;

    @Column(name = "grade_a")
    private Integer gradeA;

    @Column(name = "grade_b")
    private Integer gradeB;

    @Column(name = "grade_c")
    private Integer gradeC;

    @Column(name = "grade_d")
    private Integer gradeD;
}