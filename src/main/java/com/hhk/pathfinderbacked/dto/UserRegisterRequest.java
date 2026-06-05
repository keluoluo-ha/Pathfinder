package com.hhk.pathfinderbacked.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank(message = "姓名不能为空")
    private String name;
    @NotBlank(message = "手机号不能为空")
    private String mobile;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotNull(message = "科类不能为空")
    @Min(value = 1, message = "科类只能为1或2")
    @Max(value = 2, message = "科类只能为1或2")
    private Integer subjectType;
    @NotNull(message = "分数不能为空")
    private Integer score;
    @NotNull(message = "位次不能为空")
    private Integer rankNo;
}
