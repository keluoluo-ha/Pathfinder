package com.hhk.pathfinderbacked.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    @Bean
    public Caffeine<Object, Object> caffeineBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(30, TimeUnit.MINUTES);
    }
}
