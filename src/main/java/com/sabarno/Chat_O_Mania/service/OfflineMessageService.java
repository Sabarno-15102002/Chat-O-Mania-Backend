package com.sabarno.Chat_O_Mania.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages per-user offline queues stored at key offline_msgs:{userId}
 * Values stored are JSON (object) representations of Message DTOs (or the DTO object itself).
 */
@Service
public class OfflineMessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String OFFLINE_PREFIX = "offline_msgs:";

    public OfflineMessageService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void queueMessageForUser(String userId, Object messagePayload) {
        String key = OFFLINE_PREFIX + userId;
        redisTemplate.opsForList().rightPush(key, messagePayload);
    }

    /**
     * Pulls and deletes queued messages for a user.
     */
    public List<Object> pullQueuedMessagesForUser(String userId) {
        String key = OFFLINE_PREFIX + userId;
        List<Object> msgs = redisTemplate.opsForList().range(key, 0, -1);
        if (msgs != null && !msgs.isEmpty()) {
            redisTemplate.delete(key);
        }
        return msgs;
    }

    public long getQueuedCount(String userId) {
        String key = OFFLINE_PREFIX + userId;
        Long size = redisTemplate.opsForList().size(key);
        return size == null ? 0L : size;
    }
}
