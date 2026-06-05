package com.hhk.pathfinderbacked.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long expireAt;
    private UserProfileVO profile;
}
