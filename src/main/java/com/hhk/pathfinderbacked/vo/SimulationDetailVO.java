package com.hhk.pathfinderbacked.vo;

import lombok.Data;

import java.util.List;

@Data
public class SimulationDetailVO {
    private SimulationRecordVO record;
    private List<SimulationDetailItemVO> details;
}
