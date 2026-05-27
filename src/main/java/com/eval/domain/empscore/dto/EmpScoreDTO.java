package com.eval.domain.empscore.dto;

import lombok.Data;

public class EmpScoreDTO {

    @Data
    public static class StaffInfo {
        private String empNo;
        private String empName;
        private String deptName;
        private String jobTitle;
        private String position;

        private Double perfSelf;    // 성과 0차 (참고)
        private Double perfFirst;   // 성과 1차
        private String perfStatus;

        private Double compSelf;    // 역량 0차 (참고)
        private Double compFirst;   // 역량 1차
        private String compStatus;

        private Double totalScore;
        private String evalGrade;
    }

    @Data
    public static class LeaderInfo {
        private String empNo;
        private String empName;
        private String deptName;
        private String jobTitle;
        private String position;

        private Double multiAvgScore;
        private String multiStatus;

        private String evalGrade;
    }
}