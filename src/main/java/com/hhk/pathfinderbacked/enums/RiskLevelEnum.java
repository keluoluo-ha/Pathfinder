package com.hhk.pathfinderbacked.enums;

import lombok.Getter;

@Getter
public enum RiskLevelEnum {
    CHONG(1, "冲"),
    WEN(2, "稳"),
    BAO(3, "保");

    private final int code;
    private final String desc;

    RiskLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
