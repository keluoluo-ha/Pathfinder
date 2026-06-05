package com.hhk.pathfinderbacked.forum.dto;

import lombok.Data;

@Data
public class ForumWsClientMessage {
    private String type;
    private Object payload;
}
