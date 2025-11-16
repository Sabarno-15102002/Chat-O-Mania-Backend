package com.sabarno.chatomania.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sabarno.chatomania.entity.Chat;
import com.sabarno.chatomania.entity.Message;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.MessageException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.repository.MessageRepository;
import com.sabarno.chatomania.request.SendMessageRequest;
import com.sabarno.chatomania.service.ChatService;
import com.sabarno.chatomania.service.MessageService;
import com.sabarno.chatomania.service.UserService;

@Service
public class MessageServiceImpl implements MessageService{

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Override
    public Message sendMessage(SendMessageRequest request) throws ChatException, UserException {
        
        User user = userService.findUserById(request.getUserId());
        Chat chat = chatService.findChatById(request.getChatId());
        
        Message message = new Message();
        message.setChat(chat);
        message.setSender(user);
        message.setTimestamp(LocalDateTime.now());
        message.setContent(request.getContent());
        messageRepository.save(message);
        return message;
    }

    @Override
    public List<Message> getChatsMessages(UUID chatId, User reqUser) throws ChatException {

        Chat chat = chatService.findChatById(chatId);
        if(!chat.getParticipants().contains(reqUser)){
            throw new ChatException("Chat is not accesible to User");
        }
        List<Message> messages = messageRepository.findByChatId(chatId);
        return messages;
    }

    @Override
    public Message findMessageById(UUID messageId) throws MessageException {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new MessageException("Message Not Found with id:" + messageId));

        return message;
    }

    @Override
    public void deleteMessage(UUID messageId, User reqUser) throws MessageException {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new MessageException("Message Not Found with id:" + messageId));

        if(message.getSender().getId().equals(reqUser.getId())){
            messageRepository.deleteById(messageId);
        }
        throw new MessageException("User cannot delete this message");
    }

}
