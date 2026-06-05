package com.hhk.pathfinderbacked.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumMessageVO {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private ForumAuthorVO fromAuthor;
    private ForumAuthorVO toAuthor;
    private String content;
    private Integer type;
    private Integer isRead;
    private Long refId;
    private LocalDateTime createTime;
}
