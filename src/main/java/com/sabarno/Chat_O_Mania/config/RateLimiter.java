package com.sabarno.Chat_O_Mania.config;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;

public class RateLimiter {
  private final StringRedisTemplate redisTemplate;

  public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

  public boolean isAllowed(String key, int limit, Duration window) {
    String redisKey = "rate:" + key;

    Long count = redisTemplate.opsForValue().increment(redisKey);
    if (count != null && count == 1) {
      redisTemplate.expire(redisKey, window);
    }
    return count != null && count <= limit;
  }
}
