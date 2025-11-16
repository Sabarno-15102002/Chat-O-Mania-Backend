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

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;


    @PostMapping("/create/single")
    public ResponseEntity<Chat> createChatHandler(
        @RequestBody SingleChatRequest request,
        @RequestHeader("Authorization") String token
    ) throws UserException{
        User reqUser = userService.findUserProfile(token);
        
        Chat chat = chatService.createChat(reqUser, request.getUserId());

        return new ResponseEntity<>(chat, HttpStatus.CREATED);
    }

    @PostMapping("/create/group")
    public ResponseEntity<Chat> createGroupHandler(
        @RequestBody GroupChatRequest request,
        @RequestHeader("Authorization") String token
    ) throws UserException{
        User reqUser = userService.findUserProfile(token);
        
        Chat chat = chatService.createGroupChat(request, reqUser);

        return new ResponseEntity<>(chat, HttpStatus.CREATED);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<Chat> findChatByIdHandler(@PathVariable UUID chatId, @RequestHeader("Authorization") String token) throws ChatException{
        Chat chat = chatService.findChatById(chatId);
        return new ResponseEntity<>(chat, HttpStatus.ACCEPTED);
    }

    
    @GetMapping("/user/chats")
    public ResponseEntity<List<Chat>> findAllChatsByUserHandler(@RequestHeader("Authorization") String token) throws ChatException, UserException{
        User user = userService.findUserProfile(token);
        List<Chat> chats = chatService.findAllChatsByUserId(user.getId());
        return new ResponseEntity<>(chats, HttpStatus.ACCEPTED);
    }

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

    @DeleteMapping("/delete/{chatId}")
    public ResponseEntity<ApiResponse> deleteChat(@PathVariable UUID chatId, @RequestHeader("Authorization") String token) throws ChatException, UserException{
        User user = userService.findUserProfile(token);
        chatService.deleteChat(chatId, user);

        ApiResponse res = new ApiResponse();
        res.setMessage("Chat Successfully Deleted");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
