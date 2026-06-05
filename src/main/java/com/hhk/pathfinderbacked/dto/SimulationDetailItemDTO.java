package com.hhk.pathfinderbacked.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SimulationDetailItemDTO {
    @NotNull(message = "志愿顺序不能为空")
    @Min(value = 1, message = "志愿顺序必须大于0")
    private Integer volunteerOrder;
    @NotBlank(message = "院校代码不能为空")
    private String schoolCode;
    @NotBlank(message = "院校名称不能为空")
    private String schoolName;
    @NotBlank(message = "专业名称不能为空")
    private String majorName;
    private BigDecimal predictedProbability;
    @NotNull(message = "风险等级不能为空")
    @Min(value = 1, message = "风险等级必须为1-3")
    @Max(value = 3, message = "风险等级必须为1-3")
    private Integer riskLevel;
}
