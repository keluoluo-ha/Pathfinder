package com.hhk.pathfinderbacked.ai.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    private final AskHumanTool askHumanTool;
    private final AdmissionRecommendTool admissionRecommendTool;

    @Bean
    public ToolCallback[] allTools() {
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        DateTimeTools dateTimeTools = new DateTimeTools();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        return ToolCallbacks.from(
                askHumanTool,
                admissionRecommendTool,
                webSearchTool,
                dateTimeTools,
                webScrapingTool
        );
    }
}
