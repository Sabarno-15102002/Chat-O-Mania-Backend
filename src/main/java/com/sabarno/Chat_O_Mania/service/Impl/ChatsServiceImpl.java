package com.sabarno.Chat_O_Mania.service.Impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sabarno.Chat_O_Mania.dto.AccessChatRequestDto;
import com.sabarno.Chat_O_Mania.dto.ChatDto;
import com.sabarno.Chat_O_Mania.dto.CreateGroupDto;
import com.sabarno.Chat_O_Mania.dto.FetchChatDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.Chats;
import com.sabarno.Chat_O_Mania.entity.Message;
import com.sabarno.Chat_O_Mania.entity.User;
import com.sabarno.Chat_O_Mania.repository.ChatRepository;
import com.sabarno.Chat_O_Mania.repository.UserRepository;
import com.sabarno.Chat_O_Mania.service.IChatsService;

@Service
public class ChatsServiceImpl implements IChatsService {

  @Autowired
  ChatRepository chatRepository;

  @Autowired
  UserRepository userRepository;

  @Override
  public List<FetchChatDto> getChats(UUID userId) {
    Optional<Chats> chats = chatRepository.findByUsers_Id(userId);

    List<FetchChatDto> chatList = chats.stream()
        .sorted(Comparator.comparing(chat -> chat.getLatestMessages().stream()
            .map(Message::getCreatedAt)
            .max(Comparator.naturalOrder())
            .orElse(null), Comparator.nullsLast(Comparator.reverseOrder())))
        .map(chat -> {
          List<Message> latestMessageList = chat.getLatestMessages();

          for (Message latestMessage : latestMessageList) {
            if (latestMessage != null && latestMessage.getSender() != null) {
              User sender = userRepository.findById(latestMessage.getSender().getId()).orElse(null);
              if (sender != null) {
                latestMessage.setSender(sender);
              }
            }
          }

          List<UserDto> userDtos = chat.getUsers().stream()
              .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getMobileNumber(),
                  user.getIsAdmin()))
              .collect(Collectors.toList());

          // Ensure group admins are also converted to UserDto
          List<UserDto> groupAdminDtos = chat.getGroupAdmins().stream()
              .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getMobileNumber(),
                  user.getIsAdmin()))
              .collect(Collectors.toList());

          return new FetchChatDto(
              chat.getId(),
              chat.getChatName(),
              chat.getIsGroupChat(),
              userDtos,
              latestMessageList,
              groupAdminDtos);
        })
        .collect(Collectors.toList());

    return chatList;
  }

  @Override
  public ChatDto accessChat(UUID requestingUserId, AccessChatRequestDto targetUserId) {
    try {
      List<Chats> chats = chatRepository.findOneToOneChat(requestingUserId, targetUserId.getTargetUserId());
      List<UserDto> userDtos = new ArrayList<>();
      if (!chats.isEmpty()) {
        Chats chat = chats.get(0);
        List<User> users = chat.getUsers();
         userDtos = users.stream()
            .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getMobileNumber(),
                user.getIsAdmin()))
            .toList();
      }

      User user1 = userRepository.findById(requestingUserId).orElseThrow();
      User user2 = userRepository.findById(targetUserId.getTargetUserId()).orElseThrow();

      Chats newChat = new Chats();
      newChat.setChatName("sender");
      newChat.setIsGroupChat(false);
      newChat.setUsers(List.of(user1, user2));

      chatRepository.save(newChat);

      List<User> users = newChat.getUsers();
      userDtos = users.stream()
          .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getMobileNumber(),
              user.getIsAdmin()))
          .toList();
      return new ChatDto(newChat.getId(), newChat.getChatName(), newChat.getIsGroupChat(), userDtos,
          newChat.getLatestMessages());
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error accessing chat: " + e.getMessage());
    }
  }

  @Override
  public Chats createGroupChat(CreateGroupDto dto, UUID adminUuid) {
    try {
      if (dto.getUserIds() == null || dto.getChatName() == null || dto.getUserIds().isEmpty()) {
        throw new IllegalArgumentException("Please fill all the fields");
      }

      if (dto.getUserIds().size() < 2) {
        throw new IllegalArgumentException("More than 2 users are required to form a group chat");
      }
      List<User> users = userRepository.findAllById(dto.getUserIds());
      if (users.size() != dto.getUserIds().size()) {
        throw new IllegalArgumentException("Some users not found");
      }
      User admin = userRepository.findById(adminUuid)
          .orElseThrow(() -> new RuntimeException("Admin user not found"));

      users.add(admin);

      Chats chat = new Chats();
      chat.setChatName(dto.getChatName());
      chat.setUsers(users);
      chat.setIsGroupChat(true);
      chat.setGroupAdmins(new ArrayList<>(List.of(admin)));
      chat.setChatDescription(dto.getChatDescription());

      Chats savedChat = chatRepository.save(chat);

      return savedChat;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error Creating Group" + e.getMessage());
    }
  }

  @Override
  public Boolean renameGroupChat(UUID chatId, String newName, UUID adminUuid) {
    if (newName == null || newName.isEmpty()) {
      throw new IllegalArgumentException("Chat name cannot be empty");
    }
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new RuntimeException("Chat not found"));

      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      if (!chat.getIsGroupChat()) {
        throw new RuntimeException("This is not a group chat");
      }

      chat.setChatName(newName);
      chatRepository.save(chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error renaming group chat: " + e.getMessage());
    }
  }

  @Override
  public Boolean addToGroupChat(UUID chatId, UUID userId, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new RuntimeException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new RuntimeException("This is not a group chat");
      }
      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
      if (chat.getUsers().contains(user)) {
        throw new RuntimeException("User already exists in the group chat");
      }

      chat.getUsers().add(user);
      chatRepository.save(chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error adding to group chat: " + e.getMessage());
    }
  }

  @Override
  public Boolean removeFromGroupChat(UUID chatId, UUID userId, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new RuntimeException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new RuntimeException("This is not a group chat");
      }
      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
      if (!chat.getUsers().contains(user)) {
        throw new RuntimeException("User does not in the group chat");
      }

      chat.getUsers().remove(user);
      chat.getGroupAdmins().removeIf(admin -> admin.getId().equals(userId));
      chatRepository.save(chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error adding to group chat: " + e.getMessage());
    }
  }

  @Override
  public Boolean addAsAdmin(UUID chatId, UUID userId, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new RuntimeException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new RuntimeException("This is not a group chat");
      }
      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
      if (!chat.getUsers().contains(user)) {
        throw new RuntimeException("User does not in the group chat");
      }

      if (chat.getGroupAdmins().contains(user)) {
        throw new RuntimeException("User is already an admin");
      }

      chat.getGroupAdmins().add(user);
      chatRepository.save(chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error adding as admin: " + e.getMessage());
    }
  }

  @Override
  public Boolean removeAsAdmin(UUID chatId, UUID userId, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new RuntimeException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new RuntimeException("This is not a group chat");
      }
      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
      if (!chat.getUsers().contains(user)) {
        throw new RuntimeException("User does not in the group chat");
      }

      if (!chat.getGroupAdmins().contains(user)) {
        throw new RuntimeException("User is not an admin");
      }

      chat.getGroupAdmins().removeIf(admin -> admin.getId().equals(userId));
      chatRepository.save(chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error removing as admin: " + e.getMessage());
    }
  }

  @Override
  public Boolean leaveGroupChat(UUID chatId, UUID userId) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new RuntimeException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new RuntimeException("This is not a group chat");
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
      if (!chat.getUsers().contains(user)) {
        throw new RuntimeException("User does not in the group chat");
      }

      chat.getUsers().remove(user);
      chat.getGroupAdmins().removeIf(admin -> admin.getId().equals(userId));
      chatRepository.save(chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error leaving group chat: " + e.getMessage());
    }
  }

  @Override
  public Chats getGroupChatInfo(UUID chatId, UUID userId) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new RuntimeException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new RuntimeException("This is not a group chat");
      }
      if (!chat.getUsers().stream().anyMatch(user -> user.getId().equals(userId))) {
        throw new RuntimeException("User does not in the group chat");
      }

      return chat;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error fetching group chat info: " + e.getMessage());
    }
  }

  @Override
  public Boolean updateGroupChatDescription(UUID chatId, String description, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new RuntimeException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new RuntimeException("This is not a group chat");
      }
      chat.setChatDescription(description);
      chatRepository.save(chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error updating group chat description: " + e.getMessage());
    }
  }
}
