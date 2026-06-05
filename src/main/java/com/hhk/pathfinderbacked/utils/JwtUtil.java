package com.hhk.pathfinderbacked.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.hhk.pathfinderbacked.common.BusinessException;
import com.hhk.pathfinderbacked.common.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours}")
    private Long expireHours;

    public String createToken(Long userId, Integer subjectType) {
        long expireAt = LocalDateTime.now()
                .plusHours(expireHours)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("subjectType", subjectType);
        payload.put("exp", expireAt);
        return JWTUtil.createToken(payload, secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long parseUserId(String token) {
        JWT jwt = parseAndVerify(token);
        return Convert.toLong(jwt.getPayload("userId"));
    }

    public Integer parseSubjectType(String token) {
        JWT jwt = parseAndVerify(token);
        return Convert.toInt(jwt.getPayload("subjectType"));
    }

    public long getExpireAt(String token) {
        JWT jwt = parseAndVerify(token);
        return Convert.toLong(jwt.getPayload("exp"));
    }

    public void verifyToken(String token) {
        parseAndVerify(token);
    }

    private JWT parseAndVerify(String token) {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        boolean verified = JWTUtil.verify(token, key);
        if (!verified) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        JWT jwt = JWTUtil.parseToken(token).setKey(key);
        Long expireAt = Convert.toLong(jwt.getPayload("exp"));
        if (expireAt == null || System.currentTimeMillis() > expireAt) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        return jwt;
    }
}
