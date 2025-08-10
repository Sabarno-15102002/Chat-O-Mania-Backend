package com.sabarno.Chat_O_Mania.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sabarno.Chat_O_Mania.dto.MessageDto;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks online users in Redis set "online_users".
 * Stored values are user identifier strings (must match Principal.getName()).
 */
@Service
public class PresenceService {

    private static final String ONLINE_USERS_KEY = "online_users";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate simpleMessagingTemplate;

    public PresenceService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void markUserOnline(String userId) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
    }

    public void markUserOffline(String userId) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
    }

    public boolean isUserOnline(String userId) {
        Boolean member = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId);
        return member != null && member;
    }

    public Set<Object> getOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
    }

    public void onUserOnline(UUID userId) {
        String queueKey = "pending:" + userId;
        List<Object> pendingMessages = redisTemplate.opsForList().range(queueKey, 0, -1);

        if (pendingMessages != null && !pendingMessages.isEmpty()) {
            for (Object obj : pendingMessages) {
                MessageDto msg = (MessageDto) obj;
                simpleMessagingTemplate.convertAndSend(
                        "/topic/chat/" + msg.getChatId() + "/messages", msg);
            }
            redisTemplate.delete(queueKey);

            // Send "last notification" that all pending messages were delivered
            simpleMessagingTemplate.convertAndSend(
                    "/topic/user/" + userId + "/notifications",
                    "All pending messages delivered" + " at " + Instant.now().toString());
        }
    }

}
