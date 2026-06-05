package com.hhk.pathfinderbacked.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SimulationSaveRequest {
    @NotNull(message = "分数不能为空")
    private Integer score;
    @NotNull(message = "位次不能为空")
    @Min(value = 1, message = "位次必须大于0")
    private Integer rankNo;
    @NotNull(message = "批次不能为空")
    @Min(value = 1, message = "批次只能为1或2")
    @Max(value = 2, message = "批次只能为1或2")
    private Integer batch;
    @NotBlank(message = "模拟方案名称不能为空")
    private String simulationName;
    @NotEmpty(message = "志愿明细不能为空")
    @Valid
    private List<SimulationDetailItemDTO> details;
}
