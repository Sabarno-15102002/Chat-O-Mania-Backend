package com.sabarno.Chat_O_Mania.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sabarno.Chat_O_Mania.dto.DeleteMessageRequestDto;
import com.sabarno.Chat_O_Mania.dto.EditMessageRequestDto;
import com.sabarno.Chat_O_Mania.dto.MessageDto;
import com.sabarno.Chat_O_Mania.dto.SendMessageRequestDto;
import com.sabarno.Chat_O_Mania.entity.Chats;
import com.sabarno.Chat_O_Mania.repository.ChatRepository;
import com.sabarno.Chat_O_Mania.service.IMessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

  @Autowired
  private IMessageService messageService;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  private ChatRepository chatRepository;

  @GetMapping("/")
  public ResponseEntity<List<MessageDto>> getAllMessages(@RequestParam UUID chatId) {
    List<MessageDto> messages = messageService.getAllMessages(chatId);
    return ResponseEntity.ok(messages);
  }

  @PostMapping(value = "/", consumes = "multipart/form-data")
  public ResponseEntity<Boolean> sendMessage(@ModelAttribute SendMessageRequestDto request, Principal principal) {
    UUID senderId = UUID.fromString(principal.getName());
    MessageDto savedMessage = messageService.sendMessage(senderId, request);
    if (savedMessage == null) {
      return ResponseEntity.status(500).body(false);
    }
    List<UUID> recipients = messageService.getRecipientsInChat(request.getChatId());

    Chats chat = chatRepository.findById(request.getChatId())
        .orElseThrow(() -> new RuntimeException("Chat not found with id: " + request.getChatId()));

    if (chat.getIsGroupChat()) {
      messagingTemplate.convertAndSend(
          "/topic/chats/" + chat.getId(),
          savedMessage);
    } else {
      for (UUID recipientId : recipients) {
        if (!recipientId.equals(senderId)) {
          messagingTemplate.convertAndSendToUser(
              recipientId.toString(),
              "/queue/messages",
              savedMessage);
        }
      }
    }
    return ResponseEntity.ok(true);
  }

  @PutMapping("/edit")
  public ResponseEntity<?> editMessage(@RequestBody EditMessageRequestDto request, Principal principal) {
    UUID senderId = UUID.fromString(principal.getName());
    MessageDto updated = messageService.editMessage(senderId, request);
    if (updated != null) {
        return ResponseEntity.ok(updated);
    } else {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Edit not allowed. Only sender can edit within 5 minutes.");
    }
  }

  @DeleteMapping("/delete")
  public ResponseEntity<Boolean> deleteMessage(@RequestBody DeleteMessageRequestDto request, Principal principal) {
    UUID senderId = UUID.fromString(principal.getName());
    boolean success = messageService.deleteMessage(senderId, request);
    if (!success) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }
    return ResponseEntity.ok(success);
  }
}
