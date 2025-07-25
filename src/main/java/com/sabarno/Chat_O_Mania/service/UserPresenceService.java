package com.sabarno.Chat_O_Mania.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sabarno.Chat_O_Mania.dto.PresenceUpdateDto;

@Service
public class UserPresenceService {
    private final Map<UUID, Boolean> online = new ConcurrentHashMap<>();
    @Autowired 
    private SimpMessagingTemplate broker;

    public void setUserOnline(UUID id) {
        online.put(id, true);
        broker.convertAndSend("/topic/presence/" + id, new PresenceUpdateDto(id, true));
    }

    public void setUserOffline(UUID id) {
        online.put(id, false);
        broker.convertAndSend("/topic/presence/" + id, new PresenceUpdateDto(id, false));
    }

    public Set<UUID> getOnlineUsers() {
        return online.entrySet().stream().filter(Map.Entry::getValue)
            .map(Map.Entry::getKey).collect(Collectors.toSet());
    }
}
