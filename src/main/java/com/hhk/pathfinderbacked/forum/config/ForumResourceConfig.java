package com.hhk.pathfinderbacked.forum.config;

import com.hhk.pathfinderbacked.forum.support.ForumFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ForumResourceConfig implements WebMvcConfigurer {

    private final ForumFileStorage forumFileStorage;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = forumFileStorage.rootDir().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/forum-files/**")
                .addResourceLocations(location);
    }
}
