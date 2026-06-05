package com.hhk.pathfinderbacked.interceptor;

import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int LIMIT_PER_MINUTE = 120;
    private static final Map<String, WindowCounter> COUNTER_MAP = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = resolveKey(request);
        long minute = Instant.now().getEpochSecond() / 60;
        WindowCounter counter = COUNTER_MAP.computeIfAbsent(key, k -> new WindowCounter(minute));
        synchronized (counter) {
            if (counter.window() != minute) {
                counter = new WindowCounter(minute);
                COUNTER_MAP.put(key, counter);
            }
            if (counter.count().incrementAndGet() > LIMIT_PER_MINUTE) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "请求过于频繁，请稍后再试");
            }
        }
        return true;
    }

    private String resolveKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        return ip + ":" + uri;
    }

    private record WindowCounter(long window, AtomicInteger count) {
        private WindowCounter(long window) {
            this(window, new AtomicInteger(0));
        }
    }
}
