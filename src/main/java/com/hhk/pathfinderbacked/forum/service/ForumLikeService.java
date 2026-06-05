package com.hhk.pathfinderbacked.forum.service;

public interface ForumLikeService {
    void like(Long postId, Long userId);

    void unlike(Long postId, Long userId);
}
