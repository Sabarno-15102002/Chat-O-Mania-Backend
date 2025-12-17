package com.sabarno.chatomania.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sabarno.chatomania.entity.Chat;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.repository.ChatRepository;
import com.sabarno.chatomania.request.GroupChatRequest;
import com.sabarno.chatomania.service.ChatService;
import com.sabarno.chatomania.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService{

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserService userService;

    @Override
    public Chat createChat(User reqUser, UUID otherUserId) throws UserException {
        User otherUser = userService.findUserById(otherUserId);

        Chat existingChat = chatRepository.findSingleChatByUserIds(reqUser, otherUser);
        if(existingChat != null) {
            return existingChat;
        }
        log.info("Creating new chat between {} and {}", reqUser.getName(), otherUser.getName());

        Chat newChat = new Chat();
        newChat.setCreatedBy(reqUser);
        newChat.getParticipants().add(reqUser);
        newChat.getParticipants().add(otherUser);
        newChat.setGroup(false);
        newChat.setChatName(otherUser.getName());
        newChat.setCreatedAt(LocalDateTime.now());
        log.info("Created chat: {}", newChat);
        chatRepository.save(newChat);
        return newChat;
    }

    @Override
    public Chat findChatById(UUID chatId) throws ChatException {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatException("Chat not found"));
        return chat;
    }

    @Override
    public List<Chat> findAllChatsByUserId(UUID userId) throws UserException {
        User user = userService.findUserById(userId);
        List<Chat> chats = chatRepository.findChatsByUserId(user.getId());
        return chats;
    }

    @Override
    public Chat createGroupChat(GroupChatRequest req, User reqUser) throws UserException {
        Chat newGroupChat = new Chat();
        newGroupChat.setChatName(req.getGroupName());
        newGroupChat.setGroup(true);
        newGroupChat.setDescription(req.getDescription());
        newGroupChat.setCreatedBy(reqUser);
        newGroupChat.getAdmins().add(reqUser);
        newGroupChat.setCreatedAt(LocalDateTime.now());
        newGroupChat.getParticipants().add(reqUser);

        for(UUID userId : req.getUserIds()) {
            User user = userService.findUserById(userId);
            newGroupChat.getParticipants().add(user);
        }

        log.info("New group created by {} with name {}", reqUser.getName(), req.getGroupName());
        log.info("Created chat: {}", newGroupChat);
        chatRepository.save(newGroupChat);
        return newGroupChat;
    }

    @Override
    public Chat addUserToGroup(UUID userId, UUID chatId, User reqUser) throws ChatException, UserException {
        Chat groupChat = findChatById(chatId);
        if(!groupChat.isGroup()) {
            throw new ChatException("Cannot add user to a single chat");
        }

        if(!groupChat.getAdmins().contains(reqUser)) {
            throw new UserException("Only admins can add users to the group");
        }

        User userToAdd = userService.findUserById(userId);
        groupChat.getParticipants().add(userToAdd);
        chatRepository.save(groupChat);
        return groupChat;
    }

    @Override
    public Chat renameGroupChat(String newName, UUID chatId, User reqUser) throws ChatException, UserException {
        Chat groupChat = findChatById(chatId);
        if(!groupChat.isGroup()) {
            throw new ChatException("Cannot rename a single chat");
        }

        if(!groupChat.getAdmins().contains(reqUser)) {
            throw new UserException("Only admins can rename the group");
        }

        groupChat.setChatName(newName);
        chatRepository.save(groupChat);
        return groupChat;
    }

    @Override
    public Chat updateGroupDescription(String newDescription, UUID chatId, User reqUser) throws ChatException, UserException {
        Chat groupChat = findChatById(chatId);
        if(!groupChat.isGroup()) {
            throw new ChatException("Cannot rename a single chat");
        }

        if(!groupChat.getAdmins().contains(reqUser)) {
            throw new UserException("Only admins can rename the group");
        }

        groupChat.setDescription(newDescription);
        chatRepository.save(groupChat);
        return groupChat;
    }

    @Override
    public Chat removeUserFromGroup(UUID userId, UUID chatId, User reqUser) throws ChatException, UserException {
        Chat groupChat = findChatById(chatId);
        if(!groupChat.isGroup()) {
            throw new ChatException("Cannot remove user from a single chat");
        }

        if(!groupChat.getAdmins().contains(reqUser)) {
            throw new UserException("Only admins can remove users from the group");
        }

        User userToRemove = userService.findUserById(userId);
        groupChat.getParticipants().remove(userToRemove);
        chatRepository.save(groupChat);
        return groupChat;
    }

    @Override
    public void deleteChat(UUID chatId, User reqUser) throws ChatException, UserException {
        Chat chat = findChatById(chatId);
        if(chat.isGroup() && !chat.getCreatedBy().equals(reqUser)) {
            throw new UserException("Only the creator can delete the group");
        }
        chatRepository.delete(chat);
    }
}
