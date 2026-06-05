package com.hhk.pathfinderbacked.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("university")
public class University {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String schoolCode;
    private String schoolName;
    private String shortName;
    private String province;
    private String city;
    private String schoolType;
    private String levelTags;
    private String nature;
    private Integer isDoubleFirstClass;
    private Integer is985;
    private Integer is211;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
