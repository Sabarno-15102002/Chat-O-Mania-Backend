package com.sabarno.Chat_O_Mania.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sabarno.Chat_O_Mania.dto.AccessChatRequestDto;
import com.sabarno.Chat_O_Mania.dto.ChatDto;
import com.sabarno.Chat_O_Mania.dto.CreateGroupDto;
import com.sabarno.Chat_O_Mania.dto.FetchChatDto;
import com.sabarno.Chat_O_Mania.entity.Chats;
import com.sabarno.Chat_O_Mania.service.IChatsService;

@RestController
@RequestMapping("/api/chats")
public class ChatsController {

  @Autowired
  private IChatsService chatsService;

  @GetMapping("/")
  public ResponseEntity<List<FetchChatDto>> getChats(Principal principal) {
    UUID userId = UUID.fromString(principal.getName());
    List<FetchChatDto> chats = chatsService.getChats(userId);
    return ResponseEntity.ok(chats);
  }

  @PostMapping("/")
  public ResponseEntity<ChatDto> accessChat(@RequestBody AccessChatRequestDto targetUserId, Principal principal) {
    UUID requestingUserId = UUID.fromString(principal.getName());
    ChatDto chatDto = chatsService.accessChat(requestingUserId, targetUserId);
    return ResponseEntity.ok(chatDto);
  }

  @PostMapping("/group/create")
  public ResponseEntity<Chats> createGroupChat(@RequestBody CreateGroupDto dto, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Chats groupChat = chatsService.createGroupChat(dto, adminUuid);
    return ResponseEntity.ok(groupChat);
  }

  @PutMapping("/group/rename")
  public ResponseEntity<Boolean> renameGroupChat(@RequestParam UUID chatId, @RequestParam String newName,
      Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Boolean isRenamed = chatsService.renameGroupChat(chatId, newName, adminUuid);
    if (!isRenamed) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    return ResponseEntity.ok(isRenamed);
  }

  @PutMapping("/group/add")
  public ResponseEntity<Boolean> addToGroupChat(@RequestParam UUID chatId, @RequestParam UUID userId, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Boolean isAdded = chatsService.addToGroupChat(chatId, userId, adminUuid);
    if (!isAdded) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    return ResponseEntity.ok(isAdded);
  }

  @PutMapping("/group/remove")
  public ResponseEntity<Boolean> removeFromGroupChat(@RequestParam UUID chatId, @RequestParam UUID userId, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Boolean isRemoved = chatsService.removeFromGroupChat(chatId, userId, adminUuid);
    if (!isRemoved) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    return ResponseEntity.ok(isRemoved);
  }

  @PutMapping("/group/addadmin")
  public ResponseEntity<Boolean> addAsAdmin(@RequestParam UUID chatId, @RequestParam UUID userId, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Boolean isAdded = chatsService.addAsAdmin(chatId, userId, adminUuid);
    if (!isAdded) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    return ResponseEntity.ok(isAdded);
  }

  @PutMapping("/group/removeadmin")
  public ResponseEntity<Boolean> removeAsAdmin(@RequestParam UUID chatId, @RequestParam UUID userId, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Boolean isAdded = chatsService.removeAsAdmin(chatId, userId, adminUuid);
    if (!isAdded) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    return ResponseEntity.ok(isAdded);
  }

  @PutMapping("/group/leave")
  public ResponseEntity<Boolean> leaveGroupChat(@RequestParam UUID chatId, Principal principal) {
    UUID userId = UUID.fromString(principal.getName());
    Boolean isLeft = chatsService.leaveGroupChat(chatId, userId);
    if (!isLeft) {
      return ResponseEntity.status(403).body(false); // Forbidden if not part of the group
    }
    return ResponseEntity.ok(isLeft);
  }

  @GetMapping("/group/info")
  public ResponseEntity<Chats> getGroupChatInfo(@RequestParam UUID chatId, Principal principal) {
    UUID userId = UUID.fromString(principal.getName());
    Chats groupChat = chatsService.getGroupChatInfo(chatId, userId);
    if (groupChat == null) {
      return ResponseEntity.status(404).build(); // Not found
    }
    return ResponseEntity.ok(groupChat);
  }

  @PutMapping("/group/update-description")
  public ResponseEntity<Boolean> updateGroupDescription(@RequestParam UUID chatId, @RequestParam String description, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Chats groupChat = chatsService.getGroupChatInfo(chatId, adminUuid);
    if (groupChat == null || !groupChat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    groupChat.setChatDescription(description);
    Boolean isUpdated = chatsService.updateGroupChatDescription(chatId, description, adminUuid);
    if (!isUpdated) {
      return ResponseEntity.status(400).body(false); // Bad request if update failed
    }
    return ResponseEntity.ok(isUpdated);
  }
}
