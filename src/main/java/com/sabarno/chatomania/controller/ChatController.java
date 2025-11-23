package com.sabarno.chatomania.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabarno.chatomania.entity.Chat;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.request.GroupChatRequest;
import com.sabarno.chatomania.request.SingleChatRequest;
import com.sabarno.chatomania.response.ApiResponse;
import com.sabarno.chatomania.service.ChatService;
import com.sabarno.chatomania.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/chats")
@Tag(name = "Chat Controller", description = "APIs for managing chats and groups")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Create a new single chat", description = "Creates a new single chat between the requesting user and another user")
    @PostMapping("/create/single")
    public ResponseEntity<Chat> createChatHandler(
        @RequestBody SingleChatRequest request,
        @RequestHeader("Authorization") String token
    ) throws UserException{
        User reqUser = userService.findUserProfile(token);
        
        Chat chat = chatService.createChat(reqUser, request.getUserId());

        return new ResponseEntity<>(chat, HttpStatus.CREATED);
    }

    @Operation(summary = "Create a new group chat", description = "Creates a new group chat with the requesting user as admin")
    @PostMapping("/create/group")
    public ResponseEntity<Chat> createGroupHandler(
        @RequestBody GroupChatRequest request,
        @RequestHeader("Authorization") String token
    ) throws UserException{
        User reqUser = userService.findUserProfile(token);
        
        Chat chat = chatService.createGroupChat(request, reqUser);

        return new ResponseEntity<>(chat, HttpStatus.CREATED);
    }

    @Operation(summary = "Find chat by ID", description = "Retrieves a chat by its unique ID")
    @GetMapping("/{chatId}")
    public ResponseEntity<Chat> findChatByIdHandler(@PathVariable UUID chatId, @RequestHeader("Authorization") String token) throws ChatException{
        Chat chat = chatService.findChatById(chatId);
        return new ResponseEntity<>(chat, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Find all chats for a user", description = "Retrieves all chats associated with the requesting user")
    @GetMapping("/user/chats")
    public ResponseEntity<List<Chat>> findAllChatsByUserHandler(@RequestHeader("Authorization") String token) throws ChatException, UserException{
        User user = userService.findUserProfile(token);
        List<Chat> chats = chatService.findAllChatsByUserId(user.getId());
        return new ResponseEntity<>(chats, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Add user to group chat", description = "Adds a user to an existing group chat")
    @PutMapping("/{chatId}/add/{userId}")
    public ResponseEntity<Chat> addUserToGroup(
        @PathVariable UUID chatId,
        @PathVariable UUID userId,
        @RequestHeader("Authorization") String token
    ) throws UserException, ChatException{
        User user = userService.findUserProfile(token);
        Chat chat = chatService.addUserToGroup(userId, chatId, user);
        return new ResponseEntity<>(chat, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Remove user from group chat", description = "Removes a user from an existing group chat")
    @PutMapping("/{chatId}/remove/{userId}")
    public ResponseEntity<Chat> removeUserToGroup(
        @PathVariable UUID chatId,
        @PathVariable UUID userId,
        @RequestHeader("Authorization") String token
    ) throws UserException, ChatException{
        User user = userService.findUserProfile(token);
        Chat chat = chatService.removeUserFromGroup(userId, chatId, user);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @Operation(summary = "Rename group chat", description = "Renames an existing group chat")
    @PatchMapping("/update/{chatId}")
    public ResponseEntity<Chat> renameGroupHandler(
        @PathVariable UUID chatId,
        @RequestBody String newName,
        @RequestHeader("Authorization") String token
    ) throws ChatException, UserException{
        User user = userService.findUserProfile(token);
        Chat chat = chatService.renameGroupChat(newName, chatId, user);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @Operation(summary = "Delete a chat", description = "Deletes an existing chat by its ID")
    @DeleteMapping("/delete/{chatId}")
    public ResponseEntity<ApiResponse> deleteChat(@PathVariable UUID chatId, @RequestHeader("Authorization") String token) throws ChatException, UserException{
        User user = userService.findUserProfile(token);
        chatService.deleteChat(chatId, user);

        ApiResponse res = new ApiResponse();
        res.setMessage("Chat Successfully Deleted");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
