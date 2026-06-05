package com.hhk.pathfinderbacked.forum.websocket;

import cn.hutool.json.JSONUtil;
import com.hhk.pathfinderbacked.forum.dto.ForumPrivateMessageRequest;
import com.hhk.pathfinderbacked.forum.dto.ForumWsClientMessage;
import com.hhk.pathfinderbacked.forum.dto.ForumWsPayload;
import com.hhk.pathfinderbacked.forum.enums.ForumWsMessageTypeEnum;
import com.hhk.pathfinderbacked.forum.service.ForumMessageService;
import com.hhk.pathfinderbacked.forum.vo.ForumUnreadCountVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForumWebSocketHandler extends TextWebSocketHandler {

    private final WsSessionManager wsSessionManager;
    private final ForumMessagePushService pushService;
    private final ForumMessageService forumMessageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            session.close();
            return;
        }
        wsSessionManager.addSession(userId, session);
        ForumUnreadCountVO unread = forumMessageService.unreadCount(userId);
        pushService.pushToUser(userId, ForumWsMessageTypeEnum.UNREAD_COUNT, unread);
        Map<String, Object> onlinePayload = new HashMap<>();
        onlinePayload.put("userId", userId);
        onlinePayload.put("onlineCount", wsSessionManager.onlineCount());
        pushService.broadcastExcept(userId, ForumWsMessageTypeEnum.ONLINE_NOTIFY, onlinePayload);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            return;
        }
        ForumWsClientMessage clientMsg;
        try {
            clientMsg = JSONUtil.toBean(message.getPayload(), ForumWsClientMessage.class);
        } catch (Exception e) {
            pushService.pushToUser(userId, ForumWsMessageTypeEnum.ERROR, Map.of("message", "消息格式错误"));
            return;
        }
        if (clientMsg.getType() == null) {
            return;
        }
        String type = clientMsg.getType().toUpperCase();
        if (ForumWsMessageTypeEnum.PING.name().equals(type)) {
            session.sendMessage(new TextMessage(JSONUtil.toJsonStr(
                    ForumWsPayload.of(ForumWsMessageTypeEnum.PONG.name(), Map.of()))));
            return;
        }
        if (ForumWsMessageTypeEnum.PRIVATE_MSG.name().equals(type)) {
            Map<?, ?> payload = clientMsg.getPayload() instanceof Map ? (Map<?, ?>) clientMsg.getPayload() : Map.of();
            ForumPrivateMessageRequest req = new ForumPrivateMessageRequest();
            req.setToUserId(Long.valueOf(String.valueOf(payload.get("toUserId"))));
            req.setContent(String.valueOf(payload.get("content")));
            forumMessageService.sendPrivateMessage(userId, req);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            wsSessionManager.removeSession(userId, session);
        }
    }

    private Long getUserId(WebSocketSession session) {
        Object val = session.getAttributes().get(WsAuthHandshakeInterceptor.ATTR_USER_ID);
        return val instanceof Long l ? l : null;
    }
}
