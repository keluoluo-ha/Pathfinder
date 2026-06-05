package com.hhk.pathfinderbacked.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("score_rank_mapping")
public class ScoreRankMapping {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer year;
    private Integer subjectType;
    private Integer score;
    private Integer rankNo;
    private LocalDateTime createTime;
}
