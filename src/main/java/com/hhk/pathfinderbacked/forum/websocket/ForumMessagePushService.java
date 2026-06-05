package com.hhk.pathfinderbacked.forum.websocket;

import cn.hutool.json.JSONUtil;
import com.hhk.pathfinderbacked.forum.dto.ForumWsPayload;
import com.hhk.pathfinderbacked.forum.enums.ForumWsMessageTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumMessagePushService {

    private final WsSessionManager wsSessionManager;

    public void pushToUser(Long userId, ForumWsMessageTypeEnum type, Object payload) {
        if (userId == null) {
            return;
        }
        String text = JSONUtil.toJsonStr(ForumWsPayload.of(type.name(), payload));
        for (WebSocketSession session : wsSessionManager.getSessions(userId)) {
            sendText(session, text);
        }
    }

    public void broadcast(ForumWsMessageTypeEnum type, Object payload) {
        String text = JSONUtil.toJsonStr(ForumWsPayload.of(type.name(), payload));
        for (Long userId : wsSessionManager.onlineUserIds()) {
            for (WebSocketSession session : wsSessionManager.getSessions(userId)) {
                sendText(session, text);
            }
        }
    }

    public void broadcastExcept(Long excludeUserId, ForumWsMessageTypeEnum type, Object payload) {
        String text = JSONUtil.toJsonStr(ForumWsPayload.of(type.name(), payload));
        for (Long userId : wsSessionManager.onlineUserIds()) {
            if (excludeUserId != null && excludeUserId.equals(userId)) {
                continue;
            }
            for (WebSocketSession session : wsSessionManager.getSessions(userId)) {
                sendText(session, text);
            }
        }
    }

    private void sendText(WebSocketSession session, String text) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            log.warn("WebSocket send failed: {}", e.getMessage());
        }
    }
}
