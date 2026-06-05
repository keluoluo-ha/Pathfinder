package com.hhk.pathfinderbacked.forum.service;

import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.forum.dto.ForumPostCreateRequest;
import com.hhk.pathfinderbacked.forum.vo.ForumPostVO;

public interface ForumPostService {
    Long createPost(Long userId, ForumPostCreateRequest request);

    PageResult<ForumPostVO> listPosts(Long boardId, String category, String grade, Integer gaokaoType,
                                      String subjects, Long pageNo, Long pageSize, Long currentUserId);

    ForumPostVO detail(Long postId, Long currentUserId);

    void deletePost(Long postId, Long userId);
}
