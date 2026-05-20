package com.eval.domain.multi;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Setter
@Getter
@Entity
@Table(
	    name = "eval_category_summary_52",
	    uniqueConstraints = {
	        @UniqueConstraint(columnNames = {"evaluatee_no", "eval_type_id", "category_name"})
	    }
	)
	public class EvalCategorySummary {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "evaluatee_no", nullable = false, length = 50)
	    private String evaluateeNo;

	    @Column(name = "eval_type_id", nullable = false)
	    private Long evalTypeId;

	    @Column(name = "category_name", nullable = false, length = 100)
	    private String categoryName;

	    @Column(name = "score", nullable = false, precision = 5, scale = 2)
	    private BigDecimal score;
	    
	    @Column(name = "evaluated_at", nullable = false)
	    private LocalDateTime evaluatedAt = LocalDateTime.now();
	    
	    @Column(name = "avg_score", precision = 5, scale = 2)
	    private BigDecimal avgScore;

	}