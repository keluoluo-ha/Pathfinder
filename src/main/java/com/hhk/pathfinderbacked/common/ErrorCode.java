package com.hhk.pathfinderbacked.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    PARAM_ERROR(40000, "参数错误"),
    UNAUTHORIZED(40100, "未登录或登录已失效"),
    FORBIDDEN(40300, "无权限访问"),
    NOT_FOUND(40400, "资源不存在"),
    SYSTEM_ERROR(50000, "系统异常"),
    MOBILE_EXISTS(41001, "手机号已注册"),
    LOGIN_FAILED(41002, "手机号或密码错误"),
    SUBJECT_TYPE_ERROR(41003, "科类参数错误"),
    SCORE_MAPPING_NOT_FOUND(41004, "未找到分数对应位次"),
    SIMULATION_NOT_FOUND(41005, "模拟方案不存在"),
    CACHE_EXPIRED(41006, "缓存已过期"),
    FORUM_BOARD_NOT_FOUND(42001, "板块不存在"),
    FORUM_POST_NOT_FOUND(42002, "帖子不存在"),
    FORUM_COMMENT_NOT_FOUND(42003, "评论不存在"),
    FORUM_ALREADY_LIKED(42004, "已经点赞过了"),
    FORUM_NOT_LIKED(42005, "尚未点赞"),
    FORUM_MATERIAL_NOT_FOUND(42006, "资料不存在"),
    FORUM_MESSAGE_NOT_FOUND(42007, "消息不存在"),
    FORUM_FORBIDDEN(42008, "无权操作");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
