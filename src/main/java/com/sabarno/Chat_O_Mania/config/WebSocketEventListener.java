package com.sabarno.Chat_O_Mania.config;

import com.sabarno.Chat_O_Mania.service.OfflineMessageService;
import com.sabarno.Chat_O_Mania.service.PendingAckService;
import com.sabarno.Chat_O_Mania.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final OfflineMessageService offlineMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PendingAckService pendingAckService;

    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        String userId = principal.getName();
        presenceService.markUserOnline(userId);

        // Deliver queued messages with unique IDs for acknowledgment
        List<Object> queued = offlineMessageService.pullQueuedMessagesForUser(userId);
        if (queued != null && !queued.isEmpty()) {
            for (Object msg : queued) {
                String messageId = UUID.randomUUID().toString();
                messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/messages",
                    Map.of("id", messageId, "content", msg)
                );
                // Optionally store messageId to wait for ACK
                pendingAckService.track(userId, messageId);
            }
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                "You have " + queued.size() + " unread messages."
            );
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
