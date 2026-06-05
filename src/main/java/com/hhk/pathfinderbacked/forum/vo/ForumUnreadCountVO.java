package com.hhk.pathfinderbacked.forum.vo;

import lombok.Data;

@Data
public class ForumUnreadCountVO {
    private Long total;
    private Long privateCount;
    private Long notifyCount;
}
