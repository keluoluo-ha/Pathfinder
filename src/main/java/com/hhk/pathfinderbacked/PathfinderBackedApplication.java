package com.hhk.pathfinderbacked;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@MapperScan({"com.hhk.pathfinderbacked.mapper", "com.hhk.pathfinderbacked.forum.mapper"})
@SpringBootApplication
public class PathfinderBackedApplication {

    public static void main(String[] args) {
        SpringApplication.run(PathfinderBackedApplication.class, args);
    }

}
