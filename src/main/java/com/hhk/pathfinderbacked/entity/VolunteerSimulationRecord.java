package com.hhk.pathfinderbacked.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("volunteer_simulation_record")
public class VolunteerSimulationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Integer score;
    private Integer rankNo;
    private Integer subjectType;
    private Integer batch;
    private String simulationName;
    private Integer strategyType;
    private LocalDateTime createTime;
}
