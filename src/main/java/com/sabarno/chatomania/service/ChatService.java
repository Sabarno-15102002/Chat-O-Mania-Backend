package com.sabarno.chatomania.service;

import java.util.List;
import java.util.UUID;

import com.sabarno.chatomania.entity.Chat;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.request.GroupChatRequest;

public interface ChatService {

    public Chat createChat(User reqUser, UUID otherUserId) throws UserException;
    public Chat findChatById(UUID chatId) throws ChatException;
    public List<Chat> findAllChatsByUserId(UUID userId) throws UserException;
    public Chat createGroupChat(GroupChatRequest req, User reqUser) throws UserException;
    public Chat addUserToGroup(UUID userId, UUID chatId, User reqUser) throws ChatException, UserException;
    public Chat renameGroupChat(String newName, UUID chatId, User reqUser) throws ChatException, UserException;
    public Chat removeUserFromGroup(UUID userId, UUID chatId, User reqUser) throws ChatException, UserException;
    public void deleteChat(UUID chatId, User reqUser) throws ChatException, UserException;
}
