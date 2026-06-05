package com.hhk.pathfinderbacked.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.forum.dto.ForumCommentCreateRequest;
import com.hhk.pathfinderbacked.forum.entity.ForumComment;
import com.hhk.pathfinderbacked.forum.entity.ForumPost;
import com.hhk.pathfinderbacked.forum.mapper.ForumCommentMapper;
import com.hhk.pathfinderbacked.forum.mapper.ForumPostMapper;
import com.hhk.pathfinderbacked.forum.service.ForumCommentService;
import com.hhk.pathfinderbacked.forum.service.ForumMessageService;
import com.hhk.pathfinderbacked.forum.support.ForumUserSupport;
import com.hhk.pathfinderbacked.forum.vo.ForumCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumCommentServiceImpl implements ForumCommentService {

    private final ForumCommentMapper forumCommentMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumUserSupport forumUserSupport;
    private final ForumMessageService forumMessageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long postId, Long userId, ForumCommentCreateRequest request) {
        forumUserSupport.requireStudent(userId);
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null || post.getStatus() != 0) {
            throw new BusinessException(ErrorCode.FORUM_POST_NOT_FOUND);
        }
        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        comment.setContent(request.getContent());
        comment.setStatus(0);
        forumCommentMapper.insert(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        forumPostMapper.updateById(post);
        forumMessageService.notifyComment(userId, post.getUserId(), postId, comment.getId(), comment.getContent());
        return comment.getId();
    }

    @Override
    public PageResult<ForumCommentVO> listComments(Long postId, Long pageNo, Long pageSize) {
        Page<ForumComment> page = forumCommentMapper.selectPage(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<ForumComment>()
                        .eq(ForumComment::getPostId, postId)
                        .eq(ForumComment::getStatus, 0)
                        .orderByAsc(ForumComment::getCreateTime));
        List<ForumCommentVO> records = page.getRecords().stream().map(this::toVo).toList();
        return new PageResult<>(page.getTotal(), pageNo, pageSize, records);
    }

    private ForumCommentVO toVo(ForumComment comment) {
        ForumCommentVO vo = BeanUtil.copyProperties(comment, ForumCommentVO.class);
        vo.setAuthor(forumUserSupport.loadAuthor(comment.getUserId()));
        return vo;
    }
}
