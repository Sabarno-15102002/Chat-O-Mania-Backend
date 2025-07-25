package com.sabarno.Chat_O_Mania.controller;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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

  @MessageMapping("/typing")
  public void handleTyping(@Payload TypingStatusDto typing) {
    messagingTemplate.convertAndSendToUser(
        typing.getTo().toString(), "/queue/typing", typing);
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
