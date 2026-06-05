package com.hhk.pathfinderbacked.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SimulationRecordVO {
    private Long id;
    private Integer score;
    private Integer rankNo;
    private Integer subjectType;
    private Integer batch;
    private String simulationName;
    private Integer strategyType;
    private LocalDateTime createTime;
}
