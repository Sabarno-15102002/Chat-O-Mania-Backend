package com.sabarno.Chat_O_Mania.controller;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.sabarno.Chat_O_Mania.dto.TypingStatusDto;
import com.sabarno.Chat_O_Mania.service.IUserService;
import com.sabarno.Chat_O_Mania.service.UserPresenceService;

@Controller
public class WebSocketController {

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  private UserPresenceService upService;

  @Autowired
  private IUserService userService;

  /**
   * Accepts typing events at /app/typing and broadcasts to /topic/typing.{chatId}
   * Payload shape (client): { "chatId": "uuid", "senderId": "uuid", "typing": true }
   */
  @MessageMapping("/typing")
  public void handleTyping(TypingStatusDto typingStatus) {
    if (typingStatus.getChatId() == null || typingStatus.getFrom() == null) {
        return; // invalid payload
    }

    String destination = "/topic/typing." + typingStatus.getChatId();

    // Broadcast the full typing info to everyone in the chat
    messagingTemplate.convertAndSend(destination, typingStatus);
  }

  @GetMapping("/api/presence/online")
  public Set<UUID> online() {
    return upService.getOnlineUsers();
  }

  @GetMapping("/api/presence/last-seen/{id}")
  public ResponseEntity<Instant> lastSeen(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.getLastSeen(id));
  }
}
