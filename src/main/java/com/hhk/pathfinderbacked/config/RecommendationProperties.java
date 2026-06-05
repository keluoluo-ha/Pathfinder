package com.hhk.pathfinderbacked.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pathfinder.recommendation")
public class RecommendationProperties {
    private Integer chongLimit = 15;
    private Integer wenLimit = 15;
    private Integer baoLimit = 10;
    private Integer redisTtlMinutes = 120;
    private Integer dbCacheHours = 12;
}
