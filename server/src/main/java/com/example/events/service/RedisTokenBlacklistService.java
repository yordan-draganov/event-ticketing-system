package com.example.events.service;

import com.example.events.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.RedisCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RedisTokenBlacklistService {

    private static final String prefix = "blacklist:token:";
    private static final Logger logger = LoggerFactory.getLogger(RedisTokenBlacklistService.class);

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
                logger.debug("Token blacklisted with TTL: {} seconds", ttlInSeconds);
            } else {
                logger.warn("Token already expired");
            }
        } catch (ExpiredJwtException e) {
            logger.debug("Token already expired, skipping blacklist: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT token, using default TTL: {}", e.getMessage());
            String key = prefix + token;
            redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofHours(24));
        } catch (Exception e) {
            logger.error("Failed to extract token expiration", e);
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
        AtomicLong count = new AtomicLong(0);
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(prefix + "*")
                    .count(100)
                    .build();
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                cursor.next();
                count.incrementAndGet();
            }
            cursor.close();
            return null;
        });
        return count.get();
    }

    public void clearAllBlacklisted() {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(prefix + "*")
                    .count(100)
                    .build();
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                connection.del(cursor.next());
            }
            cursor.close();
            return null;
        });
    }
}