package com.eval.domain.empscore.service;

import com.eval.domain.empscore.dto.EmpScoreDTO;
import com.eval.domain.empscore.mapper.EmpScoreMapper;
import com.eval.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpScoreService {

    private final EmpScoreMapper mapper;

    public List<Integer> getYears() {
        return mapper.selectYears();
    }

    public List<EmpScoreDTO.StaffInfo> getStaffList(Integer year, String period, String empNo, String deptId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();
        boolean isAdmin = "ADMIN".equals(userDetails.getRole()) || userDetails.isExecutive();
        return mapper.selectStaffList(year, period, empNo, deptId, currentEmpNo, isAdmin);
    }

    public List<EmpScoreDTO.LeaderInfo> getLeaderList(Integer year, String period, String empNo, String deptId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String currentEmpNo = auth.getName();
        boolean isAdmin = "ADMIN".equals(userDetails.getRole()) || userDetails.isExecutive();
        return mapper.selectLeaderList(year, period, empNo, deptId, currentEmpNo, isAdmin);
    }
}