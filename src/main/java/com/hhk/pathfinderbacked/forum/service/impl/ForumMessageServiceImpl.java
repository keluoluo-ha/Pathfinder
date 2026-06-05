package com.hhk.pathfinderbacked.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.common.PageResult;
import com.hhk.pathfinderbacked.forum.dto.ForumPrivateMessageRequest;
import com.hhk.pathfinderbacked.forum.entity.ForumMessage;
import com.hhk.pathfinderbacked.forum.enums.ForumMessageTypeEnum;
import com.hhk.pathfinderbacked.forum.enums.ForumWsMessageTypeEnum;
import com.hhk.pathfinderbacked.forum.mapper.ForumMessageMapper;
import com.hhk.pathfinderbacked.forum.service.ForumMessageService;
import com.hhk.pathfinderbacked.forum.support.ForumUserSupport;
import com.hhk.pathfinderbacked.forum.vo.ForumMessageVO;
import com.hhk.pathfinderbacked.forum.vo.ForumUnreadCountVO;
import com.hhk.pathfinderbacked.forum.websocket.ForumMessagePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ForumMessageServiceImpl implements ForumMessageService {

    private final ForumMessageMapper forumMessageMapper;
    private final ForumUserSupport forumUserSupport;
    private final ForumMessagePushService pushService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendPrivateMessage(Long fromUserId, ForumPrivateMessageRequest request) {
        forumUserSupport.requireStudent(fromUserId);
        forumUserSupport.requireStudent(request.getToUserId());
        if (fromUserId.equals(request.getToUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能给自己发私信");
        }
        ForumMessage msg = buildMessage(fromUserId, request.getToUserId(), request.getContent(),
                ForumMessageTypeEnum.PRIVATE.getCode(), null);
        forumMessageMapper.insert(msg);
        ForumMessageVO vo = toVo(msg);
        pushService.pushToUser(request.getToUserId(), ForumWsMessageTypeEnum.PRIVATE_MSG, vo);
        pushUnread(request.getToUserId());
        pushUnread(fromUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyComment(Long fromUserId, Long toUserId, Long postId, Long commentId, String content) {
        if (toUserId == null || toUserId.equals(fromUserId)) {
            return;
        }
        String brief = content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content;
        ForumMessage msg = buildMessage(fromUserId, toUserId, "你的帖子收到新评论：" + brief,
                ForumMessageTypeEnum.COMMENT_NOTIFY.getCode(), commentId);
        forumMessageMapper.insert(msg);
        Map<String, Object> payload = new HashMap<>();
        payload.put("postId", postId);
        payload.put("commentId", commentId);
        payload.put("message", toVo(msg));
        pushService.pushToUser(toUserId, ForumWsMessageTypeEnum.NEW_COMMENT, payload);
        pushUnread(toUserId);
    }

    @Override
    public void notifyNewPost(Long fromUserId, Long postId, String title) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("postId", postId);
        payload.put("title", title);
        payload.put("fromUserId", fromUserId);
        pushService.broadcastExcept(fromUserId, ForumWsMessageTypeEnum.NEW_POST, payload);
    }

    @Override
    public PageResult<ForumMessageVO> listMessages(Long userId, Integer type, Long peerUserId,
                                                   Long pageNo, Long pageSize) {
        LambdaQueryWrapper<ForumMessage> wrapper = new LambdaQueryWrapper<ForumMessage>()
                .and(w -> w.eq(ForumMessage::getToUserId, userId).or().eq(ForumMessage::getFromUserId, userId))
                .orderByDesc(ForumMessage::getCreateTime);
        if (type != null) {
            wrapper.eq(ForumMessage::getType, type);
        }
        if (peerUserId != null) {
            wrapper.and(w -> w
                    .nested(n -> n.eq(ForumMessage::getFromUserId, userId).eq(ForumMessage::getToUserId, peerUserId))
                    .or(n -> n.eq(ForumMessage::getFromUserId, peerUserId).eq(ForumMessage::getToUserId, userId)));
        }
        Page<ForumMessage> page = forumMessageMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<ForumMessageVO> records = page.getRecords().stream().map(this::toVo).toList();
        return new PageResult<>(page.getTotal(), pageNo, pageSize, records);
    }

    @Override
    public ForumUnreadCountVO unreadCount(Long userId) {
        long total = forumMessageMapper.selectCount(new LambdaQueryWrapper<ForumMessage>()
                .eq(ForumMessage::getToUserId, userId)
                .eq(ForumMessage::getIsRead, 0));
        long privateCount = forumMessageMapper.selectCount(new LambdaQueryWrapper<ForumMessage>()
                .eq(ForumMessage::getToUserId, userId)
                .eq(ForumMessage::getIsRead, 0)
                .eq(ForumMessage::getType, ForumMessageTypeEnum.PRIVATE.getCode()));
        ForumUnreadCountVO vo = new ForumUnreadCountVO();
        vo.setTotal(total);
        vo.setPrivateCount(privateCount);
        vo.setNotifyCount(total - privateCount);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long messageId) {
        ForumMessage msg = forumMessageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(ErrorCode.FORUM_MESSAGE_NOT_FOUND);
        }
        if (!userId.equals(msg.getToUserId())) {
            throw new BusinessException(ErrorCode.FORUM_FORBIDDEN);
        }
        msg.setIsRead(1);
        forumMessageMapper.updateById(msg);
        pushUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReadBatch(Long userId, Integer type) {
        LambdaUpdateWrapper<ForumMessage> update = new LambdaUpdateWrapper<ForumMessage>()
                .eq(ForumMessage::getToUserId, userId)
                .eq(ForumMessage::getIsRead, 0)
                .set(ForumMessage::getIsRead, 1);
        if (type != null) {
            update.eq(ForumMessage::getType, type);
        }
        forumMessageMapper.update(null, update);
        pushUnread(userId);
    }

    private void pushUnread(Long userId) {
        pushService.pushToUser(userId, ForumWsMessageTypeEnum.UNREAD_COUNT, unreadCount(userId));
    }

    private ForumMessage buildMessage(Long from, Long to, String content, int type, Long refId) {
        ForumMessage msg = new ForumMessage();
        msg.setFromUserId(from);
        msg.setToUserId(to);
        msg.setContent(content);
        msg.setType(type);
        msg.setIsRead(0);
        msg.setRefId(refId);
        return msg;
    }

    private ForumMessageVO toVo(ForumMessage msg) {
        ForumMessageVO vo = new ForumMessageVO();
        vo.setId(msg.getId());
        vo.setFromUserId(msg.getFromUserId());
        vo.setToUserId(msg.getToUserId());
        vo.setFromAuthor(forumUserSupport.loadAuthor(msg.getFromUserId()));
        vo.setToAuthor(forumUserSupport.loadAuthor(msg.getToUserId()));
        vo.setContent(msg.getContent());
        vo.setType(msg.getType());
        vo.setIsRead(msg.getIsRead());
        vo.setRefId(msg.getRefId());
        vo.setCreateTime(msg.getCreateTime());
        return vo;
    }
}
