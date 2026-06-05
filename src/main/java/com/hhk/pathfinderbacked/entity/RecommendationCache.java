package com.hhk.pathfinderbacked.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recommendation_cache")
public class RecommendationCache {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer score;
    private Integer rankNo;
    private Integer subjectType;
    private Integer batch;
    private String cacheKey;
    private String resultJson;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
