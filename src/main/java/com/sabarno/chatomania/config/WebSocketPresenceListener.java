package com.sabarno.chatomania.config;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.repository.UserRepository;
import com.sabarno.chatomania.service.MessageService;

public class WebSocketPresenceListener {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageService messageService;

    @EventListener
    public void handleConnect(SessionConnectEvent event) throws UserException {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Principal principal = accessor.getUser();
        if (principal == null)
            return;

        UUID userId = UUID.fromString(principal.getName()); // set earlier

        userRepository.findById(userId).ifPresent(user -> {
            user.setIsOnline(true);
            userRepository.save(user);
        });

        messageService.syncOfflineMessage(userId);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Principal principal = accessor.getUser();
        if (principal == null)
            return;

        UUID userId = UUID.fromString(principal.getName());

        userRepository.findById(userId).ifPresent(user -> {
            user.setIsOnline(false);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        });
    }

}
