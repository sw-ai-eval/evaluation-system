package com.eval.domain.multi.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eval.domain.multi.dto.MultiEvalDTO;
import com.eval.domain.multi.dto.PageResponse;
import com.eval.domain.multi.service.MultiService;
import com.eval.global.security.CustomUserDetails;

@RestController
@RequestMapping("/evaluation/multi")
public class MultiApiController {

	private final MultiService multiService;
	
    public MultiApiController(MultiService multiService) {
        this.multiService = multiService;;
    }
	
    @GetMapping("/progress")
    public PageResponse<MultiEvalDTO> progress(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period
    ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        return multiService.getMultiProgressList(
                user.getUsername(),
                user.getPosition(),
                year,
                period,
                PageRequest.of(page, size),
                user.getRole()
        );
    }
    @GetMapping("/completed")
    public PageResponse<MultiEvalDTO> completed(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String period
    ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        return multiService.getMultiCompletedList(
                user.getUsername(),
                user.getPosition(),
                year,
                period,
                PageRequest.of(page, size),
                user.getRole()
        );
    }
}