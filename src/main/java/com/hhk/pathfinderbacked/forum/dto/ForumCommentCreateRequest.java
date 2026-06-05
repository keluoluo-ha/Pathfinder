package com.hhk.pathfinderbacked.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForumCommentCreateRequest {
    @NotBlank(message = "评论内容不能为空")
    private String content;
    private Long parentId;
}
