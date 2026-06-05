package com.hhk.pathfinderbacked.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserOverviewVO {

    private Profile profile;
    private Integer starCoin;
    private BigDecimal balance;
    private Message message;
    private Exchange exchange;
    private Volunteer volunteer;
    private Planning planning;

    @Data
    public static class Profile {
        private String avatarUrl;
        private String nickname;
        private String signature;
    }

    @Data
    public static class Message {
        private Integer unreadCount;
    }

    @Data
    public static class Exchange {
        private Integer recordCount;
    }

    @Data
    public static class Volunteer {
        private BigDecimal hours;
    }

    @Data
    public static class Planning {
        private String phone;
    }
}
