package com.hhk.pathfinderbacked.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SimulationDetailItemVO {
    private Integer volunteerOrder;
    private String schoolCode;
    private String schoolName;
    private String majorName;
    private BigDecimal predictedProbability;
    private Integer riskLevel;
}
