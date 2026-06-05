package com.hhk.pathfinderbacked.vo;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String studentNo;
    private String name;
    private String avatarUrl;
    private String nickname;
    private String signature;
    private String mobile;
    private Integer subjectType;
    private Integer score;
    private Integer rankNo;
    private String province;
}
