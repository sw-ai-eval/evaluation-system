package com.eval.domain.finalgrade.dto;

import lombok.Data;
import java.util.List;

public class FinalGradeDTO {

    /** 사원 탭 목록용 */
    @Data
    public static class StaffInfo {
        private String empNo;
        private String empName;
        private String deptName;
        private String position;

        // 성과평가
        private Integer perfFirst;      // 1차 (부서장 점수)
        private Integer perfSecond;     // 2차 (임원 점수)
        private Integer perfTotal;
        private String  perfStatus;

        // 역량평가
        private Integer compFirst;      // 1차 (부서장 점수)
        private Integer compSecond;     // 2차 (임원 점수)
        private Integer compTotal;
        private String  compStatus;

        // 종합
        private Integer totalScore;
        private String  totalGrade;
        private String  confirmedGrade;
        private String  confirmStatus;
        private String  reason;
        private String  weightRatio;
    }

    /** 부서장 탭 목록용 */
    @Data
    public static class LeaderInfo {
        private String empNo;
        private String empName;
        private String deptName;
        private String position;

        // 성과평가 (1차 = 다면 avg, 2차 = 임원)
        private Double  perfFirst;      // 1차 (다면 avg_score)
        private Integer perfSecond;     // 2차 (임원 점수)
        private Integer perfTotal;
        private String  perfStatus;

        // 역량평가 (1차 = 다면 avg, 2차 = 임원) → 동일한 다면 avg
        private Double  compFirst;      // 1차 (다면 avg_score, 성과와 동일)
        private Integer compSecond;     // 2차 (임원 점수)
        private Integer compTotal;
        private String  compStatus;

        // 종합
        private Integer totalScore;
        private String  totalGrade;
        private String  confirmedGrade;
        private String  confirmStatus;
        private String  reason;
        private String  weightRatio;
    }

    /** 등급 조정 요청 */
    @Data
    public static class GradeAdjustReq {
        private String empNo;
        private String confirmedGrade;
        private String reason;
        private Integer year;
    }

    /** 최종 확정 요청 */
    @Data
    public static class ConfirmReq {
        private List<String> empNos;
        private Integer year;
    }

    /** 부서별 등급 통계 */
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