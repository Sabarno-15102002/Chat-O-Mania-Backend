package com.sabarno.Chat_O_Mania.config;

import com.sabarno.Chat_O_Mania.service.OfflineMessageService;
import com.sabarno.Chat_O_Mania.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final OfflineMessageService offlineMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        String userId = principal.getName();
        presenceService.markUserOnline(userId);

        // Deliver queued messages and notify user about unread count
        List<Object> queued = offlineMessageService.pullQueuedMessagesForUser(userId);
        if (queued != null && !queued.isEmpty()) {
            for (Object msg : queued) {
                messagingTemplate.convertAndSendToUser(userId, "/queue/messages", msg);
            }
            // send notification summary
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications",
                    "You have " + queued.size() + " unread messages.");
        }
    }

    @EventListener
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;
        String userId = principal.getName();
        presenceService.markUserOffline(userId);
    }
}
