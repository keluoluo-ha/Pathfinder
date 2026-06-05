package com.hhk.pathfinderbacked.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentNo;
    private String name;
    private String avatarUrl;
    private String nickname;
    private String signature;
    private String mobile;
    private String password;
    private Integer starCoin;
    private BigDecimal balance;
    private Integer unreadMessageCount;
    private Integer exchangeRecordCount;
    private BigDecimal volunteerHours;
    private String planningContactPhone;
    private Integer subjectType;
    private Integer score;
    private Integer rankNo;
    private String province;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
