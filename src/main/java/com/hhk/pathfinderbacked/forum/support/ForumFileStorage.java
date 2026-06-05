package com.hhk.pathfinderbacked.forum.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ForumFileStorage {

    @Value("${pathfinder.forum.upload-dir:./uploads/forum}")
    private String uploadDir;

    public Path rootDir() {
        Path path = Paths.get(uploadDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path.toAbsolutePath().normalize();
    }

    public void ensureRootDir() throws IOException {
        Files.createDirectories(rootDir());
    }

    public String toPublicUrl(String filename) {
        return "/forum-files/" + filename;
    }
}
