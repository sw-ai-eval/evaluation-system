package com.eval.domain.finalgrade.dto;

import lombok.Data;
import java.util.List;

public class FinalGradeDTO {

    /** 사원 탭
     * 성과: 0차(본인참고) / 1차(부서장) / 상태
     * 역량: 0차(본인참고) / 1차(부서장) / 상태
     */
    @Data
    public static class StaffInfo {
        private String empNo;
        private String empName;
        private String deptName;
        private String deptId;
        private String position;

        // 성과평가
        private Double perfSelf;     // 0차 (본인, 참고용)
        private Double perfFirst;    // 1차 (부서장)
        private String perfStatus;

        // 역량평가
        private Double compSelf;     // 0차 (본인, 참고용)
        private Double compFirst;    // 1차 (부서장)
        private String compStatus;

        // 종합
        private Double totalScore;
        private String totalGrade;
        private String confirmedGrade;
        private String confirmStatus;
        private String reason;
        private String weightRatio;
    }

    /** 부서장 탭
     * 다면평가: 평균점수 / 상태(N/M명 완료)
     */
    @Data
    public static class LeaderInfo {
        private String empNo;
        private String empName;
        private String deptName;
        private String deptId;
        private String position;

        // 다면평가
        private Double  multiAvgScore;   // 다면 평균점수
        private String  multiStatus;     // ex) "평가완료" or "진행중 (1/2명)"

        // 종합
        private String totalGrade;
        private String confirmedGrade;
        private String confirmStatus;
        private String reason;
        private String weightRatio;
    }

    /** 등급 재조정용 - 부서 내 사원 점수 목록 */
    @Data
    public static class StaffScore {
        private String empNo;
        private String deptId;
        private Double totalScore;
        private String currentGrade;
        private Integer confirmStatus; // 0=미확정, 1=확정
    }

    @Data
    public static class GradeAdjustReq {
        private String empNo;
        private String deptId;      // 자동 재조정용
        private String position;    // 부서장 여부 판단용
        private String confirmedGrade;
        private String reason;
        private Integer year;
    }

    @Data
    public static class ConfirmReq {
        private List<String> empNos;
        private Integer year;
    }

    @Data
    public static class DeptStat {
        private String deptName;
        private Integer total;
        private Integer cntS;
        private Integer cntA;
        private Integer cntB;
        private Integer cntC;
        private Integer cntD;
    }
}