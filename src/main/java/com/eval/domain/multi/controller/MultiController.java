package com.eval.domain.multi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MultiController{
	@GetMapping("/evaluation/multi")
	public String multiPage() { 
		
		
		return "evaluation/multi";
	}
	
}