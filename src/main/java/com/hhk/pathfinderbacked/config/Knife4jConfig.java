package com.hhk.pathfinderbacked.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    private static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PathFinder API")
                        .description("广东高考志愿模拟平台 MVP")
                        .version("v1.0.0"))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("先调用登录接口获取 token；在文档页 Authorize 中粘贴 JWT（界面会自动加 Bearer 前缀）。")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
