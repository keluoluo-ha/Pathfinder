package com.hhk.pathfinderbacked.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.forum.dto.ForumPostCreateRequest;
import com.hhk.pathfinderbacked.forum.entity.ForumBoard;
import com.hhk.pathfinderbacked.forum.entity.ForumLike;
import com.hhk.pathfinderbacked.forum.entity.ForumPost;
import com.hhk.pathfinderbacked.forum.mapper.ForumBoardMapper;
import com.hhk.pathfinderbacked.forum.mapper.ForumLikeMapper;
import com.hhk.pathfinderbacked.forum.mapper.ForumPostMapper;
import com.hhk.pathfinderbacked.forum.service.ForumMessageService;
import com.hhk.pathfinderbacked.forum.service.ForumPostService;
import com.hhk.pathfinderbacked.forum.support.ForumUserSupport;
import com.hhk.pathfinderbacked.forum.vo.ForumPostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumPostServiceImpl implements ForumPostService {

    private final ForumPostMapper forumPostMapper;
    private final ForumBoardMapper forumBoardMapper;
    private final ForumLikeMapper forumLikeMapper;
    private final ForumUserSupport forumUserSupport;
    private final ForumMessageService forumMessageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(Long userId, ForumPostCreateRequest request) {
        forumUserSupport.requireStudent(userId);
        ForumBoard board = forumBoardMapper.selectById(request.getBoardId());
        if (board == null || board.getStatus() != 0) {
            throw new BusinessException(ErrorCode.FORUM_BOARD_NOT_FOUND);
        }
        ForumPost post = new ForumPost();
        post.setBoardId(request.getBoardId());
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        post.setGrade(request.getGrade());
        post.setGaokaoType(request.getGaokaoType());
        post.setSubjects(request.getSubjects());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(0);
        forumPostMapper.insert(post);
        forumMessageService.notifyNewPost(userId, post.getId(), post.getTitle());
        return post.getId();
    }

    @Override
    public PageResult<ForumPostVO> listPosts(Long boardId, String category, String grade, Integer gaokaoType,
                                             String subjects, Long pageNo, Long pageSize, Long currentUserId) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, 0)
                .orderByDesc(ForumPost::getCreateTime);
        if (boardId != null) {
            wrapper.eq(ForumPost::getBoardId, boardId);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(ForumPost::getCategory, category);
        }
        if (StringUtils.hasText(grade)) {
            wrapper.eq(ForumPost::getGrade, grade);
        }
        if (gaokaoType != null) {
            wrapper.eq(ForumPost::getGaokaoType, gaokaoType);
        }
        if (StringUtils.hasText(subjects)) {
            wrapper.eq(ForumPost::getSubjects, subjects);
        }
        Page<ForumPost> page = forumPostMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Set<Long> likedPostIds = loadLikedPostIds(currentUserId, page.getRecords());
        List<ForumPostVO> records = page.getRecords().stream()
                .map(p -> toVo(p, likedPostIds.contains(p.getId())))
                .toList();
        return new PageResult<>(page.getTotal(), pageNo, pageSize, records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPostVO detail(Long postId, Long currentUserId) {
        ForumPost post = requirePost(postId);
        post.setViewCount(post.getViewCount() + 1);
        forumPostMapper.updateById(post);
        boolean liked = currentUserId != null && forumLikeMapper.selectCount(new LambdaQueryWrapper<ForumLike>()
                .eq(ForumLike::getPostId, postId)
                .eq(ForumLike::getUserId, currentUserId)) > 0;
        return toVo(post, liked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId) {
        ForumPost post = requirePost(postId);
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORUM_FORBIDDEN);
        }
        post.setStatus(1);
        forumPostMapper.updateById(post);
    }

    private ForumPost requirePost(Long postId) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null || post.getStatus() != 0) {
            throw new BusinessException(ErrorCode.FORUM_POST_NOT_FOUND);
        }
        return post;
    }

    private Set<Long> loadLikedPostIds(Long userId, List<ForumPost> posts) {
        if (userId == null || posts.isEmpty()) {
            return Set.of();
        }
        List<Long> postIds = posts.stream().map(ForumPost::getId).toList();
        return forumLikeMapper.selectList(new LambdaQueryWrapper<ForumLike>()
                        .eq(ForumLike::getUserId, userId)
                        .in(ForumLike::getPostId, postIds))
                .stream()
                .map(ForumLike::getPostId)
                .collect(Collectors.toSet());
    }

    private ForumPostVO toVo(ForumPost post, boolean liked) {
        ForumPostVO vo = BeanUtil.copyProperties(post, ForumPostVO.class);
        vo.setAuthor(forumUserSupport.loadAuthor(post.getUserId()));
        vo.setLiked(liked);
        ForumBoard board = forumBoardMapper.selectById(post.getBoardId());
        if (board != null) {
            vo.setBoardName(board.getName());
        }
        return vo;
    }
}
