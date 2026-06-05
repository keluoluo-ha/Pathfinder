package com.hhk.pathfinderbacked.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("major")
public class Major {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String majorCode;
    private String majorName;
    private String majorCategory;
    private String degreeType;
    private Integer durationYear;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
