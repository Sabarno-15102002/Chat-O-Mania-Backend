package com.sabarno.Chat_O_Mania.config;

import java.io.IOException;
import java.security.Principal;
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

import com.sabarno.Chat_O_Mania.service.IMessageService;
import com.sabarno.Chat_O_Mania.service.PendingAckService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PresenceWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private PendingAckService pendingAckService;

    // Tracks last heartbeat time per session
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Principal p = session.getPrincipal();
        if (p == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
            return;
        }

        UUID userId;
        try {
            userId = UUID.fromString(p.getName());
        } catch (IllegalArgumentException e) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid userId"));
            return;
        }

        sessions.put(session.getId(), session);
        lastHeartbeat.put(session.getId(), System.currentTimeMillis());
        System.out.println("User " + userId + " connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if ("PING".equals(payload)) {
            // Update heartbeat timestamp
            lastHeartbeat.put(session.getId(), System.currentTimeMillis());
            session.sendMessage(new TextMessage("PONG"));
        } else if (payload.startsWith("ACK:")) {
            // Handle message acknowledgment
            String messageId = payload.substring(4);

            Principal p = session.getPrincipal();
            if (p == null) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized ACK"));
                return;
            }

            UUID userId;
            try {
                userId = UUID.fromString(p.getName());
            } catch (IllegalArgumentException e) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid userId in Principal"));
                return;
            }

            // Mark message as delivered in DB
            messageService.markAsDelivered(userId.toString(), messageId);
            pendingAckService.remove(userId.toString(), messageId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Principal p = session.getPrincipal();
        String user = (p != null ? p.getName() : "unknown");

        sessions.remove(session.getId());
        lastHeartbeat.remove(session.getId());
        System.out.println("User " + user + " disconnected");
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
                    } catch (IOException ignored) {
                    }
                }
            }
        });
    }

    // private UUID extractUserId(WebSocketSession session) {
    //     String q = session.getUri().getQuery(); // e.g. "userId=..."
    //     return UUID.fromString(q.split("=")[1]);
    // }
}
