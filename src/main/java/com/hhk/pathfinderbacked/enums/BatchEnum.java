package com.hhk.pathfinderbacked.enums;

import lombok.Getter;

@Getter
public enum BatchEnum {
    UNDERGRADUATE(1, "本科"),
    COLLEGE(2, "专科");

    private final int code;
    private final String desc;

    BatchEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static BatchEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BatchEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }
}
