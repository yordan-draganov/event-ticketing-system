package com.example.events.service;

import com.example.events.security.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenBlacklistService {

    private static final String prefix = "blacklist:token:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    public RedisTokenBlacklistService(RedisTemplate<String, String> redisTemplate, JwtUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    public void blacklistToken(String token) {
        try {
            Date expiration = jwtUtil.extractExpiration(token);
            long ttlInSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;

            if (ttlInSeconds > 0) {
                String key = prefix + token;
                redisTemplate.opsForValue().set(key, "blacklisted", ttlInSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            String key = prefix + token;
            redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofHours(24));
        }
    }


    public boolean isTokenBlacklisted(String token) {
        String key = prefix + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // for testing
    public void removeFromBlacklist(String token) {
        String key = prefix + token;
        redisTemplate.delete(key);
    }

    // for monitoring
    public long getBlacklistSize() {
        return redisTemplate.keys(prefix + "*").size();
    }


    public void clearAllBlacklisted() {
        redisTemplate.keys(prefix + "*").forEach(redisTemplate::delete);
    }
}