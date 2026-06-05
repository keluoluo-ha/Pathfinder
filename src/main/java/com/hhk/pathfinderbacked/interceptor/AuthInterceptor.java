package com.hhk.pathfinderbacked.interceptor;

import cn.hutool.json.JSONUtil;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import com.hhk.pathfinderbacked.utils.JwtUtil;
import com.hhk.pathfinderbacked.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行浏览器 CORS 预检请求，预检不会携带 Authorization
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        boolean missingOrBlank = authHeader == null || authHeader.isBlank();
        boolean startsBearer = authHeader != null && authHeader.startsWith("Bearer ");
        // #region agent log
        agentLog("H1", "AuthInterceptor.preHandle:entry", "authorization snapshot", Map.of(
                "uri", request.getRequestURI(),
                "method", request.getMethod(),
                "missingOrBlank", missingOrBlank,
                "startsWithBearer", startsBearer,
                "headerLen", authHeader == null ? 0 : authHeader.length()
        ));
        // #endregion
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            // #region agent log
            agentLog("H1", "AuthInterceptor.preHandle:reject", "missing or not Bearer", Map.of(
                    "reason", missingOrBlank ? "missing_or_blank" : "no_bearer_prefix"
            ));
            // #endregion
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        // #region agent log
        agentLog("H3", "AuthInterceptor.preHandle:tokenLen", "after Bearer prefix", Map.of(
                "tokenLen", token.length()
        ));
        // #endregion
        String blacklistKey = "jwt:blacklist:" + token;
        boolean blacklisted = Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey));
        // #region agent log
        agentLog("H4", "AuthInterceptor.preHandle:blacklist", "redis blacklist check", Map.of(
                "blacklisted", blacklisted
        ));
        // #endregion
        if (blacklisted) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已退出，请重新登录");
        }
        try {
            jwtUtil.verifyToken(token);
        } catch (BusinessException ex) {
            // #region agent log
            String hint = ex.getMessage() != null && ex.getMessage().contains("过期") ? "expired" : "invalid_or_other";
            agentLog("H4", "AuthInterceptor.preHandle:jwt", "jwt verify threw", Map.of(
                    "bizCode", ex.getCode(),
                    "failKind", hint
            ));
            // #endregion
            throw ex;
        }
        Long uid = jwtUtil.parseUserId(token);
        UserContext.setUserId(uid);
        // #region agent log
        agentLog("H5", "AuthInterceptor.preHandle:ok", "auth passed", Map.of(
                "userId", uid
        ));
        // #endregion
        return true;
    }

    // #region agent log
    private static void agentLog(String hypothesisId, String location, String message, Map<String, ?> data) {
        try {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("sessionId", "02783e");
            line.put("timestamp", System.currentTimeMillis());
            line.put("location", location);
            line.put("message", message);
            line.put("hypothesisId", hypothesisId);
            line.put("runId", "auth-debug-2");
            line.put("data", data);
            String payload = JSONUtil.toJsonStr(line) + System.lineSeparator();
            for (Path p : debugLogPaths()) {
                try {
                    if (p.getParent() != null) {
                        Files.createDirectories(p.getParent());
                    }
                    Files.writeString(p, payload, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);
                } catch (Exception ignoredEach) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 同时写入模块 target 与仓库根目录，避免 IDE 运行目录不同导致日志落盘路径不在工作区。
     */
    private static List<Path> debugLogPaths() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        List<Path> list = new ArrayList<>(2);
        Path name = cwd.getFileName();
        if (name != null && "pathfinder-backed".equalsIgnoreCase(name.toString())) {
            list.add(cwd.resolve("target").resolve("debug-02783e.log"));
            Path parent = cwd.getParent();
            if (parent != null) {
                list.add(parent.resolve("debug-02783e.log"));
            }
            return list;
        }
        Path module = cwd.resolve("pathfinder-backed");
        if (Files.isDirectory(module)) {
            list.add(module.resolve("target").resolve("debug-02783e.log"));
            list.add(cwd.resolve("debug-02783e.log"));
            return list;
        }
        list.add(cwd.resolve("debug-02783e.log"));
        return list;
    }
    // #endregion

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
