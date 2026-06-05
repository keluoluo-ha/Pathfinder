package com.hhk.pathfinderbacked.forum.enums;

import lombok.Getter;

@Getter
public enum ForumMessageTypeEnum {
    PRIVATE(1, "私信"),
    COMMENT_NOTIFY(2, "评论提醒"),
    NEW_POST(3, "新帖通知"),
    SYSTEM(4, "系统");

    private final int code;
    private final String label;

    ForumMessageTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
