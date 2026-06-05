package com.hhk.pathfinderbacked.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumCommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private ForumAuthorVO author;
    private Long parentId;
    private String content;
    private LocalDateTime createTime;
}
