package com.hhk.pathfinderbacked.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForumMaterialCreateRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String subject;
    private String grade;
}
