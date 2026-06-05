package com.hhk.pathfinderbacked.enums;

import lombok.Getter;

@Getter
public enum SubjectTypeEnum {
    PHYSICS(1, "物理类"),
    HISTORY(2, "历史类");

    private final int code;
    private final String desc;

    SubjectTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static SubjectTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SubjectTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }
}
