package com.hhk.pathfinderbacked.controller;

import cn.hutool.core.util.StrUtil;
import com.hhk.pathfinderbacked.ai.SimpleAgentApp;
import com.hhk.pathfinderbacked.common.Result;
import com.hhk.pathfinderbacked.dto.AiChatRequest;
import com.hhk.pathfinderbacked.vo.AiChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import java.util.UUID;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Slf4j
public class AiAgentController {

    private final SimpleAgentApp simpleAgentApp;

    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        String chatId = StrUtil.blankToDefault(request.getChatId(), UUID.randomUUID().toString());
        String answer = simpleAgentApp.doChat(request.getMessage(), chatId);
        return Result.success(new AiChatResponse(chatId, answer));
    }

    @PostMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatSse(@Valid @RequestBody AiChatRequest request) {
        String chatId = StrUtil.blankToDefault(request.getChatId(), UUID.randomUUID().toString());
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().name("chatId").data(chatId));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        simpleAgentApp.doChatWithSse(request.getMessage(), chatId).subscribe(
                chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("sse chat error, chatId={}", chatId, error);
                    emitter.completeWithError(error);
                },
                () -> {
                    try {
                        emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    } catch (IOException e) {
                        log.warn("sse done event send failed, chatId={}", chatId, e);
                    }
                    emitter.complete();
                }
        );
        return emitter;
    }
}
