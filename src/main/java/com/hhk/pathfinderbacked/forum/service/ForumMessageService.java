package com.hhk.pathfinderbacked.forum.service;

import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.forum.dto.ForumPrivateMessageRequest;
import com.hhk.pathfinderbacked.forum.vo.ForumMessageVO;
import com.hhk.pathfinderbacked.forum.vo.ForumUnreadCountVO;

public interface ForumMessageService {
    void sendPrivateMessage(Long fromUserId, ForumPrivateMessageRequest request);

    void notifyComment(Long fromUserId, Long toUserId, Long postId, Long commentId, String content);

    void notifyNewPost(Long fromUserId, Long postId, String title);

    PageResult<ForumMessageVO> listMessages(Long userId, Integer type, Long peerUserId, Long pageNo, Long pageSize);

    ForumUnreadCountVO unreadCount(Long userId);

    void markRead(Long userId, Long messageId);

    void markReadBatch(Long userId, Integer type);
}
