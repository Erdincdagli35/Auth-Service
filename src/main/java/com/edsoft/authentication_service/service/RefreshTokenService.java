package com.edsoft.authentication_service.service;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;

    public RefreshTokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void store(String token, Long userId, long ttlMs) {
        String key = "refresh:" + token;
        redisTemplate.opsForValue().set(key, userId.toString(), Duration.ofMillis(ttlMs));
    }

    public Optional<Long> getUserIdFor(String token) {
        String key = "refresh:" + token;
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null) return Optional.empty();
        try {
            return Optional.of(Long.valueOf(val.toString()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public void revoke(String token) {
        redisTemplate.delete("refresh:" + token);
    }
}