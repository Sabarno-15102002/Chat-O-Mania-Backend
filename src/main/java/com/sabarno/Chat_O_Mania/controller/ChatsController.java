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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/chats")
@Tag(name = "CRUD REST APIs for Chats of Chat-O-Mania  ", description = "APIs for managing chats")
public class ChatsController {

  @Autowired
  private IChatsService chatsService;

  @Operation(summary = "Get all chats for the authenticated user", description = "Fetches all chat conversations for the currently authenticated user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved chats"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated"),
      @ApiResponse(responseCode = "404", description = "No chats found for the user")
  })
  @GetMapping("/")
  public ResponseEntity<List<FetchChatDto>> getChats(Principal principal) {
    UUID userId = UUID.fromString(principal.getName());
    List<FetchChatDto> chats = chatsService.getChats(userId);
    return ResponseEntity.ok(chats);
  }

  @Operation(summary = "Access a chat with another user", description = "Allows the authenticated user to access a chat with another user by their ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully accessed chat"),
      @ApiResponse(responseCode = "404", description = "Chat not found or user not part of the chat"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PostMapping("/")
  public ResponseEntity<ChatDto> accessChat(@RequestBody AccessChatRequestDto targetUserId, Principal principal) {
    UUID requestingUserId = UUID.fromString(principal.getName());
    ChatDto chatDto = chatsService.accessChat(requestingUserId, targetUserId);
    return ResponseEntity.ok(chatDto);
  }

  @Operation(summary = "Create a new group chat with other users", description = "Creates a new group chat conversation with other users.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully created group chat"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PostMapping("/group/create")
  public ResponseEntity<Chats> createGroupChat(@RequestBody CreateGroupDto dto, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Chats groupChat = chatsService.createGroupChat(dto, adminUuid);
    return ResponseEntity.ok(groupChat);
  }

  @Operation(summary = "Rename a group chat by its ID", description = "Renames a group chat by its unique ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully renamed group chat"),
      @ApiResponse(responseCode = "404", description = "Group chat not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
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

  @Operation(summary = "Add a user to a group chat", description = "Adds a user to an existing group chat by its ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully added user to group chat"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin of the group chat"),
      @ApiResponse(responseCode = "404", description = "Group chat not found or user not part of the chat"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PutMapping("/group/add")
  public ResponseEntity<Boolean> addToGroupChat(@RequestParam UUID chatId, @RequestParam UUID userId, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Boolean isAdded = chatsService.addToGroupChat(chatId, userId, adminUuid);
    if (!isAdded) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    return ResponseEntity.ok(isAdded);
  }

  @Operation(summary = "Remove a user from a group chat", description = "Removes a user from an existing group chat by its ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully removed user from group chat"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin of the group chat"),
      @ApiResponse(responseCode = "404", description = "Group chat not found or user not part of the chat"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PutMapping("/group/remove")
  public ResponseEntity<Boolean> removeFromGroupChat(@RequestParam UUID chatId, @RequestParam UUID userId, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Boolean isRemoved = chatsService.removeFromGroupChat(chatId, userId, adminUuid);
    if (!isRemoved) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    return ResponseEntity.ok(isRemoved);
  }

  @Operation(summary = "Add a user as an admin in a group chat", description = "Adds a user as an admin in an existing group chat by its ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully added user as admin in group chat"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin of the group chat"),
      @ApiResponse(responseCode = "404", description = "Group chat not found or user not part of the chat"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PutMapping("/group/addadmin")
  public ResponseEntity<Boolean> addAsAdmin(@RequestParam UUID chatId, @RequestParam UUID userId, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Boolean isAdded = chatsService.addAsAdmin(chatId, userId, adminUuid);
    if (!isAdded) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    return ResponseEntity.ok(isAdded);
  }

  @Operation(summary = "Remove a user as an admin in a group chat", description = "Removes a user as an admin in an existing group chat by its ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully removed user as admin in group chat"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin of the group chat"),
      @ApiResponse(responseCode = "404", description = "Group chat not found or user not part of the chat"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PutMapping("/group/removeadmin")
  public ResponseEntity<Boolean> removeAsAdmin(@RequestParam UUID chatId, @RequestParam UUID userId, Principal principal) {
    UUID adminUuid = UUID.fromString(principal.getName());
    Boolean isAdded = chatsService.removeAsAdmin(chatId, userId, adminUuid);
    if (!isAdded) {
      return ResponseEntity.status(403).body(false); // Forbidden if not admin
    }
    return ResponseEntity.ok(isAdded);
  }

  @Operation(summary = "Leave a group chat", description = "Allows a user to leave a group chat they are part of.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully left group chat"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not part of the group chat"),
      @ApiResponse(responseCode = "404", description = "Group chat not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PutMapping("/group/leave")
  public ResponseEntity<Boolean> leaveGroupChat(@RequestParam UUID chatId, Principal principal) {
    UUID userId = UUID.fromString(principal.getName());
    Boolean isLeft = chatsService.leaveGroupChat(chatId, userId);
    if (!isLeft) {
      return ResponseEntity.status(403).body(false); // Forbidden if not part of the group
    }
    return ResponseEntity.ok(isLeft);
  }

  @Operation(summary = "Get group chat information", description = "Fetches detailed information about a group chat by its ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved group chat information"),
      @ApiResponse(responseCode = "404", description = "Group chat not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @GetMapping("/group/info")
  public ResponseEntity<Chats> getGroupChatInfo(@RequestParam UUID chatId, Principal principal) {
    UUID userId = UUID.fromString(principal.getName());
    Chats groupChat = chatsService.getGroupChatInfo(chatId, userId);
    if (groupChat == null) {
      return ResponseEntity.status(404).build(); // Not found
    }
    return ResponseEntity.ok(groupChat);
  }

  @Operation(summary = "Update group chat description", description = "Updates the description of a group chat by its ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated group chat description"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin of the group chat"),
      @ApiResponse(responseCode = "404", description = "Group chat not found"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
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
