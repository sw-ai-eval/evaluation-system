package com.eval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@EnableJpaRepositories(basePackages = "com.eval.domain")
@MapperScan(basePackages = {
	    "com.eval.domain.employee.mapper",
	    "com.eval.domain.dept.mapper",
	    "com.eval.domain.performance.mapper",
	    "com.eval.domain.multi.mapper"
	    "com.eval.domain.competency.mapper"
	})
@SpringBootApplication
public class EvalSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvalSystemApplication.class, args);
	}

}
