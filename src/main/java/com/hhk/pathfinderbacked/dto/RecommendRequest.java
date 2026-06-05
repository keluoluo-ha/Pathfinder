package com.hhk.pathfinderbacked.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecommendRequest {
    @NotNull(message = "位次不能为空")
    @Min(value = 1, message = "位次必须大于0")
    private Integer rankNo;
    @NotNull(message = "批次不能为空")
    @Min(value = 1, message = "批次只能为1或2")
    @Max(value = 2, message = "批次只能为1或2")
    private Integer batch;
    @Min(value = 1, message = "冲数量必须大于0")
    private Integer chongLimit;
    @Min(value = 1, message = "稳数量必须大于0")
    private Integer wenLimit;
    @Min(value = 1, message = "保数量必须大于0")
    private Integer baoLimit;
}
