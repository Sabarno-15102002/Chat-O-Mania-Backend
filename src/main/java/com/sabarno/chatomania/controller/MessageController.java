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

import com.sabarno.chatomania.entity.Message;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.MessageException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.request.EditMessageRequest;
import com.sabarno.chatomania.request.SendMessageRequest;
import com.sabarno.chatomania.response.ApiResponse;
import com.sabarno.chatomania.service.MessageService;
import com.sabarno.chatomania.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Message Controller", description = "APIs for managing messages within chats")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Send a new message", description = "Sends a new message within a chat")
    @PostMapping("/create")
    public ResponseEntity<Message> sendMessage(@RequestBody SendMessageRequest request,
            @RequestHeader("Authorization") String token) throws UserException, ChatException {
        User user = userService.findUserProfile(token);
        request.setUserId(user.getId());
        Message message = messageService.sendMessage(request);
        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Get messages of a chat", description = "Retrieves all messages within a specific chat")
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<Message>> getChatsMessagesHandler(
            @PathVariable UUID chatId,
            @RequestHeader("Authorization") String token) throws UserException, ChatException {
        User user = userService.findUserProfile(token);
        List<Message> messages = messageService.getChatsMessages(chatId, user);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @Operation(summary = "Mark messages as seen", description = "Marks all messages in a chat as seen by the user")
    @PutMapping("/{chatId}/seen")
    public ResponseEntity<ApiResponse> markSeen(
        @PathVariable UUID chatId,
        @RequestHeader("Authorization") String token
    ) throws ChatException, UserException{
        User user = userService.findUserProfile(token);
        messageService.setMessageToSeen(chatId, user);

        ApiResponse response = new ApiResponse();
        response.setMessage("All messages are Seen");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Edit a message", description = "Edits the content of an existing message")
    @PatchMapping("/{messageId}")
    public ResponseEntity<Message> editMessage(
            @RequestBody EditMessageRequest request,
            @PathVariable UUID messageId,
            @RequestHeader("Authorization") String token) throws UserException, MessageException {
        User user = userService.findUserProfile(token);
        Message message = messageService.editMessage(messageId, request.getNewContent(), user);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @Operation(summary = "Delete a message", description = "Deletes an existing message")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse> deleteMessage(
            @PathVariable UUID messageId,
            @RequestHeader("Authorization") String token) throws UserException, MessageException {
        User user = userService.findUserProfile(token);
        messageService.deleteMessage(messageId, user);

        ApiResponse res = new ApiResponse();
        res.setMessage("Message Deleted Successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
