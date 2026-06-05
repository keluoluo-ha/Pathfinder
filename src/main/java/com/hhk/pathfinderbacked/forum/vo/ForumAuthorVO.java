package com.hhk.pathfinderbacked.forum.vo;

import lombok.Data;

@Data
public class ForumAuthorVO {
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer subjectType;
}
