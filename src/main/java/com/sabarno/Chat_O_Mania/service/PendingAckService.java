package com.sabarno.Chat_O_Mania.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PendingAckService {

    private final OfflineMessageService offlineMessageService;
    private final Map<String, Map<String, Long>> pendingAcks = new ConcurrentHashMap<>();

    public void track(String userId, String messageId) {
        pendingAcks
            .computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .put(messageId, System.currentTimeMillis());
    }

    public void remove(String userId, String messageId) {
        Map<String, Long> userPending = pendingAcks.get(userId);
        if (userPending != null) {
            userPending.remove(messageId);
        }
    }

    @Scheduled(fixedRate = 10000) // check every 10s
    public void checkForTimeouts() {
        long now = System.currentTimeMillis();

        for (var entry : pendingAcks.entrySet()) {
            String userId = entry.getKey();
            Map<String, Long> messages = entry.getValue();

            for (var msgEntry : new HashMap<>(messages).entrySet()) {
                if (now - msgEntry.getValue() > 15000) { // 15s timeout
                    String messageId = msgEntry.getKey();

                    // Re-queue for delivery
                    offlineMessageService.queueMessageForUser(userId, messages);
                    messages.remove(messageId);
                }
            }
        }
    }
}
