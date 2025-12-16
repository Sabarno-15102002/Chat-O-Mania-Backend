package com.sabarno.chatomania.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RateLimiterService {
    private static final String PREFIX = "rate:";
    private static final int MAX_REQUESTS = 50;
    private static final int WINDOW_SECONDS = 30;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean allow(String key) {
        try {
            String redisKey = PREFIX + key;

            Long count = redisTemplate.opsForValue().increment(redisKey);

            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(WINDOW_SECONDS));
            }

            if (count != null && count > MAX_REQUESTS) {
                log.warn("RATE_LIMIT_EXCEEDED key={}", key);
                return false;
            }
            return true;

        } catch (Exception e) {
            // Fail open (do not block chat if Redis is down)
            log.error("RATE_LIMIT_REDIS_ERROR key={}", key, e);
            return true;
        }
    }
}
