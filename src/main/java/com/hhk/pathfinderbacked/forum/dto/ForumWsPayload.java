package com.hhk.pathfinderbacked.forum.dto;

import lombok.Data;

@Data
public class ForumWsPayload {
    private String type;
    private Object payload;
    private Long timestamp;

    public static ForumWsPayload of(String type, Object payload) {
        ForumWsPayload p = new ForumWsPayload();
        p.setType(type);
        p.setPayload(payload);
        p.setTimestamp(System.currentTimeMillis());
        return p;
    }
}
