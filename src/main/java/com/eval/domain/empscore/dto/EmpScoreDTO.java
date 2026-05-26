package com.eval.domain.empscore.dto;

import lombok.Data;

public class EmpScoreDTO {

    /** 사원용 (성과/역량 1차=부서장) */
    @Data
    public static class StaffInfo {
        private String empNo;
        private String empName;
        private String deptName;
        private String jobTitle;
        private String position;

        private Double perfFirst;
        private Double perfSecond;
        private Double perfTotal;
        private String perfStatus;

        private Double compFirst;
        private Double compSecond;
        private Double compTotal;
        private String compStatus;

        private Double totalScore;
        private String evalGrade;
    }

    /** 부서장용 (성과/역량 1차=다면avg) */
    @Data
    public static class LeaderInfo {
        private String empNo;
        private String empName;
        private String deptName;
        private String jobTitle;
        private String position;

        private Double perfFirst;   // 다면 avg_score
        private Double perfSecond;
        private Double perfTotal;
        private String perfStatus;

        private Double compFirst;   // 다면 avg_score (동일)
        private Double compSecond;
        private Double compTotal;
        private String compStatus;

        private Double totalScore;
        private String evalGrade;
    }
}