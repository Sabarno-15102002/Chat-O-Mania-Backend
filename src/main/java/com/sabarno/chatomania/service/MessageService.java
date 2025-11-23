package com.sabarno.chatomania.service;

import java.util.List;
import java.util.UUID;

import com.sabarno.chatomania.entity.Message;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.MessageException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.request.SendMessageRequest;

public interface MessageService {

    public Message sendMessage(SendMessageRequest request) throws ChatException, UserException;
    public List<Message> getChatsMessages(UUID chatId, User reqUser) throws ChatException;
    public Message findMessageById(UUID messageId) throws MessageException;
    public Message editMessage(UUID messageId, String newContent, User reqUser) throws MessageException;
    public void deleteMessage(UUID messageId, User reqUser) throws MessageException;
    public void setMessageToSeen(UUID chatId, User reqUser) throws ChatException;
}
