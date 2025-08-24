package com.sabarno.Chat_O_Mania.controller;

import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "CRUD REST APIs for Messages of Chat-O-Mania", description = "APIs for managing messages in chats")
public class MessageController {

  @Autowired
  private IMessageService messageService;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  private ChatRepository chatRepository;

  @Operation(summary = "Get paginated messages in a chat", description = "Fetches messages for a given chat ID with pagination.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved messages"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid chat ID"),
      @ApiResponse(responseCode = "404", description = "Chat not found with the provided ID"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @GetMapping("/")
  public ResponseEntity<Page<MessageDto>> getMessages(
      @RequestParam UUID chatId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Page<MessageDto> messages = messageService.getMessages(chatId, page, size);
    return ResponseEntity.ok(messages);
  }

  @Operation(summary = "Send a message", description = "Sends a message in a chat and notifies recipients.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Message sent successfully"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid message data or chat ID"),
      @ApiResponse(responseCode = "500", description = "Internal server error - Failed to send message"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
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

  @Operation(summary = "Edit a message", description = "Allows the sender to edit their message within 5 minutes of sending.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Message edited successfully"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Only sender can edit within 5 minutes"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid message data or chat ID"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PutMapping("/edit")
  public ResponseEntity<?> editMessage(@RequestBody EditMessageRequestDto request, Principal principal) {
    UUID senderId = UUID.fromString(principal.getName());
    MessageDto updated = messageService.editMessage(senderId, request);
    if (updated != null) {
      return ResponseEntity.ok(updated);
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("Edit not allowed. Only sender can edit within 5 minutes.");
    }
  }

  @Operation(summary = "Delete a message", description = "Allows the sender to delete their message.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Message deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Message not found or not sent by the user"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid message data or chat ID"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @DeleteMapping("/delete")
  public ResponseEntity<Boolean> deleteMessage(@RequestBody DeleteMessageRequestDto request, Principal principal) {
    UUID senderId = UUID.fromString(principal.getName());
    boolean success = messageService.deleteMessage(senderId, request);
    if (!success) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }
    return ResponseEntity.ok(success);
  }

  @GetMapping("/search")
  public ResponseEntity<List<MessageDto>> searchMessages(
      @RequestParam UUID chatId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate) {

    List<MessageDto> results;

    if (keyword != null && startDate != null && endDate != null) {
      results = messageService.searchMessagesByKeywordAndDate(chatId, keyword, startDate, endDate);
    } else if (keyword != null) {
      results = messageService.searchMessagesByKeyword(chatId, keyword);
    } else if (startDate != null && endDate != null) {
      results = messageService.searchMessagesByDate(chatId, startDate, endDate);
    } else {
      results = Collections.emptyList();
    }

    return ResponseEntity.ok(results);
  }

  @PostMapping("/forward/{messageId}/to/{chatId}")
  public ResponseEntity<String> forwardMessage(@PathVariable UUID messageId, @PathVariable UUID chatId, Principal principal) {
    if(principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
    }
    UUID senderId = UUID.fromString(principal.getName());
    messageService.forwardMessage(messageId, chatId, senderId);
    return ResponseEntity.ok("Message forwarded.");
  }

}
