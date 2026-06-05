package com.hhk.pathfinderbacked.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("volunteer_simulation_detail")
public class VolunteerSimulationDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long simulationId;
    private Integer volunteerOrder;
    private String schoolCode;
    private String schoolName;
    private String majorName;
    private BigDecimal predictedProbability;
    private Integer riskLevel;
    private LocalDateTime createTime;
}
