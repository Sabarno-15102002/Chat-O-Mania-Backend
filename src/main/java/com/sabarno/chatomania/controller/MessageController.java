package com.sabarno.chatomania.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabarno.chatomania.entity.Message;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.MessageException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.request.SendMessageRequest;
import com.sabarno.chatomania.response.ApiResponse;
import com.sabarno.chatomania.service.MessageService;
import com.sabarno.chatomania.service.UserService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Message> sendMessage(@RequestBody SendMessageRequest request, @RequestHeader("Authorization") String token) throws UserException, ChatException{
        User user = userService.findUserProfile(token);
        request.setUserId(user.getId());
        Message message = messageService.sendMessage(request);
        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<Message>> getChatsMessagesHandler(
        @PathVariable UUID chatId,
        @RequestHeader("Authorization") String token
    ) throws UserException, ChatException{
        User user = userService.findUserProfile(token);
        List<Message> messages = messageService.getChatsMessages(chatId, user);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse> deleteMessage(
        @PathVariable UUID messageId,
        @RequestHeader("Authorization") String token
    ) throws UserException, MessageException{
        User user = userService.findUserProfile(token);
        messageService.deleteMessage(messageId, user);

        ApiResponse res = new ApiResponse();
        res.setMessage("Message Deleted Successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
