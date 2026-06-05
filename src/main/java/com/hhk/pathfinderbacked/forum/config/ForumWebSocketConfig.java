package com.hhk.pathfinderbacked.forum.config;

import com.hhk.pathfinderbacked.forum.websocket.ForumWebSocketHandler;
import com.hhk.pathfinderbacked.forum.websocket.WsAuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class ForumWebSocketConfig implements WebSocketConfigurer {

    private final ForumWebSocketHandler forumWebSocketHandler;
    private final WsAuthHandshakeInterceptor wsAuthHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(forumWebSocketHandler, "/ws/forum")
                .addInterceptors(wsAuthHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
