package com.eval.domain.multi;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "eval_summary_52",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_summary",
            columnNames = {"evaluatee_no", "eval_type_id"}
        )
    }
)
public class EvalSummaryChart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    private String evaluateeNo; //본인

    private Integer evalTypeId;

    private BigDecimal totalScore; // 본인 합계

    private BigDecimal avgScore; // 본인의 평균

    private BigDecimal maxGivenScore; // 사용 안하고 있음

    private LocalDateTime evaluatedAt;
}