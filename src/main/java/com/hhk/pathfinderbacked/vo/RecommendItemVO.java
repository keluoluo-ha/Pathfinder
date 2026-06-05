package com.hhk.pathfinderbacked.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecommendItemVO {
    private String schoolCode;
    private String schoolName;
    private String majorName;
    private Integer year;
    private Integer minRank;
    private BigDecimal ratio;
    private Integer riskLevel;
    private BigDecimal predictedProbability;
}
