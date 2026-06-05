package com.hhk.pathfinderbacked.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.forum.entity.ForumLike;
import com.hhk.pathfinderbacked.forum.entity.ForumPost;
import com.hhk.pathfinderbacked.forum.mapper.ForumLikeMapper;
import com.hhk.pathfinderbacked.forum.mapper.ForumPostMapper;
import com.hhk.pathfinderbacked.forum.service.ForumLikeService;
import com.hhk.pathfinderbacked.forum.support.ForumUserSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForumLikeServiceImpl implements ForumLikeService {

    private final ForumLikeMapper forumLikeMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumUserSupport forumUserSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long postId, Long userId) {
        forumUserSupport.requireStudent(userId);
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null || post.getStatus() != 0) {
            throw new BusinessException(ErrorCode.FORUM_POST_NOT_FOUND);
        }
        long exists = forumLikeMapper.selectCount(new LambdaQueryWrapper<ForumLike>()
                .eq(ForumLike::getPostId, postId)
                .eq(ForumLike::getUserId, userId));
        if (exists > 0) {
            throw new BusinessException(ErrorCode.FORUM_ALREADY_LIKED);
        }
        ForumLike like = new ForumLike();
        like.setPostId(postId);
        like.setUserId(userId);
        try {
            forumLikeMapper.insert(like);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.FORUM_ALREADY_LIKED);
        }
        post.setLikeCount(post.getLikeCount() + 1);
        forumPostMapper.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long postId, Long userId) {
        ForumLike like = forumLikeMapper.selectOne(new LambdaQueryWrapper<ForumLike>()
                .eq(ForumLike::getPostId, postId)
                .eq(ForumLike::getUserId, userId));
        if (like == null) {
            throw new BusinessException(ErrorCode.FORUM_NOT_LIKED);
        }
        forumLikeMapper.deleteById(like.getId());
        ForumPost post = forumPostMapper.selectById(postId);
        if (post != null && post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            forumPostMapper.updateById(post);
        }
    }
}
