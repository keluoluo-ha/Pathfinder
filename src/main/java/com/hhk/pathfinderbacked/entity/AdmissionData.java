package com.hhk.pathfinderbacked.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admission_data")
public class AdmissionData {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String schoolCode;
    private String schoolName;
    private String groupCode;
    private String planCount;
    private String enrollCount;
    private String minScore;
    private String minRank;
    private String source;
    private String batch;
    private String category;
    private String scrapeTime;
    private String year;
}
