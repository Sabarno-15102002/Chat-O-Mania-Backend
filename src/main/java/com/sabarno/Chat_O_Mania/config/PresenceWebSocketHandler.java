package com.sabarno.Chat_O_Mania.config;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.sabarno.Chat_O_Mania.repository.UserRepository;
import com.sabarno.Chat_O_Mania.service.UserPresenceService;

@Component
public class PresenceWebSocketHandler extends TextWebSocketHandler {
    @Autowired 
    private UserPresenceService presenceService;
    
    @Autowired 
    private UserRepository userRepo;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID userId = extractUserId(session);
        presenceService.setUserOnline(userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID userId = extractUserId(session);
        presenceService.setUserOffline(userId);
        userRepo.updateLastSeen(userId, Instant.now());
    }

    private UUID extractUserId(WebSocketSession session) {
        String q = session.getUri().getQuery(); // e.g. "userId=..."
        return UUID.fromString(q.split("=")[1]);
    }
}
