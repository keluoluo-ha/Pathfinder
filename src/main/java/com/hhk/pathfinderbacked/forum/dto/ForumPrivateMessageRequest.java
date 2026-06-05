package com.hhk.pathfinderbacked.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForumPrivateMessageRequest {
    @NotNull(message = "接收人不能为空")
    private Long toUserId;
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
