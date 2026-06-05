package com.hhk.pathfinderbacked.forum.enums;

import lombok.Getter;

@Getter
public enum ForumWsMessageTypeEnum {
    NEW_POST,
    NEW_COMMENT,
    PRIVATE_MSG,
    UNREAD_COUNT,
    ONLINE_NOTIFY,
    PING,
    PONG,
    ERROR
}
