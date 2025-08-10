package com.sabarno.Chat_O_Mania.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.sabarno.Chat_O_Mania.repository.UserRepository;
import com.sabarno.Chat_O_Mania.service.IMessageService;
import com.sabarno.Chat_O_Mania.service.PendingAckService;
import com.sabarno.Chat_O_Mania.service.UserPresenceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PresenceWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private UserPresenceService presenceService;

    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private IMessageService messageService;

    @Autowired
    private PendingAckService pendingAckService;


    // Tracks last heartbeat time per session
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID userId = extractUserId(session);
        presenceService.setUserOnline(userId);
        sessions.put(session.getId(), session);
        lastHeartbeat.put(session.getId(), System.currentTimeMillis());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        if ("PING".equals(payload)) {
            // Update heartbeat timestamp
            lastHeartbeat.put(session.getId(), System.currentTimeMillis());
            session.sendMessage(new TextMessage("PONG"));
        } 
        else if (payload.startsWith("ACK:")) {
            // Handle message acknowledgment
            String messageId = payload.substring(4);
            UUID userId = extractUserId(session);
            // Mark message as delivered in DB
            messageService.markAsDelivered(userId.toString(), messageId); // implement in your service
            pendingAckService.remove(userId.toString(), messageId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID userId = extractUserId(session);
        presenceService.setUserOffline(userId);
        userRepo.updateLastSeen(userId, Instant.now());
        lastHeartbeat.remove(session.getId());
        sessions.remove(session.getId());
    }

    @Scheduled(fixedRate = 15000) // every 15 seconds
    public void checkHeartbeats() {
        long now = System.currentTimeMillis();
        lastHeartbeat.forEach((sessionId, lastTime) -> {
            if (now - lastTime > 30000) { // No heartbeat in 30s
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        session.close(CloseStatus.SESSION_NOT_RELIABLE);
                    } catch (IOException ignored) {}
                }
            }
        });
    }

    private UUID extractUserId(WebSocketSession session) {
        String q = session.getUri().getQuery(); // e.g. "userId=..."
        return UUID.fromString(q.split("=")[1]);
    }
}

