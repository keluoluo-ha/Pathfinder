package com.hhk.pathfinderbacked.forum.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WsSessionManager {

    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void removeSession(Long userId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> set = userSessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    public Set<WebSocketSession> getSessions(Long userId) {
        CopyOnWriteArraySet<WebSocketSession> set = userSessions.get(userId);
        if (set == null) {
            return Collections.emptySet();
        }
        return Set.copyOf(set);
    }

    public Set<Long> onlineUserIds() {
        return Set.copyOf(userSessions.keySet());
    }

    public int onlineCount() {
        return userSessions.size();
    }
}
