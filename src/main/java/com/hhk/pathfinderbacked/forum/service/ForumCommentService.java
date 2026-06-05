package com.hhk.pathfinderbacked.forum.service;

import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.forum.dto.ForumCommentCreateRequest;
import com.hhk.pathfinderbacked.forum.vo.ForumCommentVO;

public interface ForumCommentService {
    Long addComment(Long postId, Long userId, ForumCommentCreateRequest request);

    PageResult<ForumCommentVO> listComments(Long postId, Long pageNo, Long pageSize);
}
