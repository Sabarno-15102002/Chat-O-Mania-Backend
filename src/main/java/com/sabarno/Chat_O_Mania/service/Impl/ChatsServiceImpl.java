package com.sabarno.Chat_O_Mania.service.Impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import com.sabarno.Chat_O_Mania.dto.AccessChatRequestDto;
import com.sabarno.Chat_O_Mania.dto.ChatDto;
import com.sabarno.Chat_O_Mania.dto.CreateGroupDto;
import com.sabarno.Chat_O_Mania.dto.FetchChatDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.Chats;
import com.sabarno.Chat_O_Mania.entity.Message;
import com.sabarno.Chat_O_Mania.entity.User;
import com.sabarno.Chat_O_Mania.exception.GroupChatOperationException;
import com.sabarno.Chat_O_Mania.exception.NotGroupChatException;
import com.sabarno.Chat_O_Mania.exception.NotValidDataException;
import com.sabarno.Chat_O_Mania.exception.ResourceNotFoundException;
import com.sabarno.Chat_O_Mania.mapper.ChatMapper;
import com.sabarno.Chat_O_Mania.mapper.UserMapper;
import com.sabarno.Chat_O_Mania.repository.ChatRepository;
import com.sabarno.Chat_O_Mania.repository.UserRepository;
import com.sabarno.Chat_O_Mania.service.IChatsService;

@Service
public class ChatsServiceImpl implements IChatsService {

  @Autowired
  ChatRepository chatRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  private RedisCacheManager cacheManager;

  /**
   * Fetches all chats for a given user, sorted by the latest message timestamp.
   *
   * @param userId the ID of the user whose chats are to be fetched
   * @return a list of FetchChatDto containing chat details
   */
  @Override
  @Cacheable(value = "allchats", key = "#userId")
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
              User sender = userRepository.findById(latestMessage.getSender().getId()).orElseThrow();
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

          return ChatMapper.mapToFetchChatDto(chat, new FetchChatDto(), userDtos, latestMessageList, groupAdminDtos);
        }).collect(Collectors.toList());

    return chatList;
  }

  /**
   * Accesses a one-to-one chat between the requesting user and the target user.
   *
   * @param requestingUserId the ID of the user requesting access
   * @param targetUserId     the ID of the target user to access the chat with
   * @return a ChatDto containing chat details
   */
  @Override
  @Cacheable(value = "oneToOneChat", key = "#requestingUserId + '-' + #targetUserId.targetUserId")
  public ChatDto accessChat(UUID requestingUserId, AccessChatRequestDto targetUserId) {
    try {
      List<Chats> chats = chatRepository.findOneToOneChat(requestingUserId, targetUserId.getTargetUserId());
      List<UserDto> userDtos = new ArrayList<>();
      if (!chats.isEmpty()) {
        Chats chat = chats.get(0);
        List<User> users = chat.getUsers();
        User targetUser = userRepository.findById(targetUserId.getTargetUserId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Target user not found with ID: " + targetUserId.getTargetUserId()));
        users.remove(targetUser);
        if(targetUser.getBlockedUsers().stream().anyMatch(user -> user.getId().equals(requestingUserId))){
          // 🚨 Hide profile pic + lastSeen if blocked
          targetUser.setProfilePicUrl(null);
          targetUser.setLastSeen(null);
        }
        users.add(targetUser);
        userDtos = users.stream()
            .map(user -> UserMapper.mapToUserDto(user, new UserDto()))
            .toList();
        return ChatMapper.mapToChatDto(chat, new ChatDto(), userDtos);
      } else {

        User user1 = userRepository.findById(requestingUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestingUserId));
        User user2 = userRepository.findById(targetUserId.getTargetUserId()).orElseThrow(
            () -> new ResourceNotFoundException("Target user not found with ID: " + targetUserId.getTargetUserId()));

        if (user1.getFriends().stream().noneMatch(friend -> friend.getId().equals(user2.getId()))) {
          throw new ResourceNotFoundException("You are not friends with this user");
        }
        Chats newChat = new Chats();
        newChat.setChatName("sender");
        newChat.setIsGroupChat(false);
        newChat.setUsers(List.of(user1, user2));

        chatRepository.save(newChat);

        List<User> users = newChat.getUsers();
        users.remove(user1);
        if(user2.getBlockedUsers().stream().anyMatch(user -> user.getId().equals(requestingUserId))){
          // 🚨 Hide profile pic + lastSeen if blocked
          user2.setProfilePicUrl(null);
          user2.setLastSeen(null);
        }
        users.add(user2);
        userDtos = users.stream()
            .map(user -> UserMapper.mapToUserDto(user, new UserDto()))
            .toList();
        cacheManager.getCache("oneToOneChat").put(requestingUserId, requestingUserId.toString() + "-" + targetUserId.getTargetUserId());
        return ChatMapper.mapToChatDto(newChat, new ChatDto(), userDtos);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error accessing chat: " + e.getMessage());
    }
  }

  /**
   * Creates a group chat with the specified users and admin.
   *
   * @param CreateGroupDto the DTO containing user IDs and chat details
   * @param adminUuid      the ID of the admin user
   * @return a ChatDto containing chat details
   */
  @Override
  public Chats createGroupChat(CreateGroupDto dto, UUID adminUuid) {
    try {
      if (dto.getUserIds() == null || dto.getChatName() == null || dto.getUserIds().isEmpty()) {
        throw new NotValidDataException("Please fill all the fields");
      }

      if (dto.getUserIds().size() < 2) {
        throw new NotValidDataException("More than 2 users are required to form a group chat");
      }
      List<User> users = userRepository.findAllById(dto.getUserIds());
      if (users.size() != dto.getUserIds().size()) {
        throw new NotValidDataException("Some users not found");
      }
      User admin = userRepository.findById(adminUuid)
          .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

      users.add(admin);

      Chats chat = new Chats();
      chat.setChatName(dto.getChatName());
      chat.setUsers(users);
      chat.setIsGroupChat(true);
      chat.setGroupAdmins(new ArrayList<>(List.of(admin)));
      chat.setChatDescription(dto.getChatDescription());

      Chats savedChat = chatRepository.save(chat);

      cacheManager.getCache("allchats").evict(adminUuid.toString());
      cacheManager.getCache("allchats").put(adminUuid, getChats(adminUuid));

      cacheManager.getCache("groupChatInfo").evict(chat.getId().toString() + "-" + adminUuid.toString());
      cacheManager.getCache("groupChatInfo").put(chat.getId().toString() + "-" + adminUuid.toString(), chat);
      return savedChat;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupChatOperationException("Error Creating Group" + e.getMessage());
    }
  }

  /**
   * Renames a group chat.
   *
   * @param chatId    the ID of the chat to rename
   * @param newName   the new name for the chat
   * @param adminUuid the ID of the admin user performing the rename
   * @return true if the rename was successful, false otherwise
   */
  @Override
  public Boolean renameGroupChat(UUID chatId, String newName, UUID adminUuid) {
    if (newName == null || newName.isEmpty()) {
      throw new NotValidDataException("Chat name cannot be empty");
    }
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      if (!chat.getIsGroupChat()) {
        throw new NotGroupChatException("This is not a group chat");
      }

      chat.setChatName(newName);
      chatRepository.save(chat);
      cacheManager.getCache("allchats").evict(adminUuid.toString());
      cacheManager.getCache("allchats").put(adminUuid, getChats(adminUuid));

      cacheManager.getCache("groupChatInfo").evict(chatId.toString() + "-" + adminUuid.toString());
      cacheManager.getCache("groupChatInfo").put(chatId.toString() + "-" + adminUuid.toString(), chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupChatOperationException("Error renaming group chat: " + e.getMessage());
    }
  }

  /**
   * Adds a user to a group chat.
   *
   * @param chatId    the ID of the chat to add the user to
   * @param userId    the ID of the user to add
   * @param adminUuid the ID of the admin user performing the operation
   * @return true if the user was added successfully, false otherwise
   */
  @Override
  public Boolean addToGroupChat(UUID chatId, UUID userId, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new NotGroupChatException("This is not a group chat");
      }
      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      if (chat.getUsers().contains(user)) {
        throw new GroupChatOperationException("User already exists in the group chat");
      }

      chat.getUsers().add(user);
      chatRepository.save(chat);
      cacheManager.getCache("allchats").evict(adminUuid.toString());
      cacheManager.getCache("allchats").put(adminUuid, getChats(adminUuid));

      cacheManager.getCache("groupChatInfo").evict(chatId.toString() + "-" + adminUuid.toString());
      cacheManager.getCache("groupChatInfo").put(chatId.toString() + "-" + adminUuid.toString(), chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupChatOperationException("Error adding to group chat: " + e.getMessage());
    }
  }

  /**
   * Removes a user from a group chat.
   *
   * @param chatId    the ID of the chat to remove the user from
   * @param userId    the ID of the user to remove
   * @param adminUuid the ID of the admin user performing the operation
   * @return true if the user was removed successfully, false otherwise
   */
  @Override
  public Boolean removeFromGroupChat(UUID chatId, UUID userId, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new NotGroupChatException("This is not a group chat");
      }
      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      if (!chat.getUsers().contains(user)) {
        throw new ResourceNotFoundException("User is not present in the group chat");
      }

      chat.getUsers().remove(user);
      chat.getGroupAdmins().removeIf(admin -> admin.getId().equals(userId));
      chatRepository.save(chat);
      cacheManager.getCache("allchats").evict(adminUuid.toString());
      cacheManager.getCache("allchats").put(adminUuid, getChats(adminUuid));

      cacheManager.getCache("groupChatInfo").evict(chatId.toString() + "-" + adminUuid.toString());
      cacheManager.getCache("groupChatInfo").put(chatId.toString() + "-" + adminUuid.toString(), chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupChatOperationException("Error adding to group chat: " + e.getMessage());
    }
  }

  /**
   * Adds a user as an admin in a group chat.
   *
   * @param chatId    the ID of the chat to add the user as admin
   * @param userId    the ID of the user to add as admin
   * @param adminUuid the ID of the admin user performing the operation
   * @return true if the user was added as admin successfully, false otherwise
   */
  @Override
  public Boolean addAsAdmin(UUID chatId, UUID userId, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new NotGroupChatException("This is not a group chat");
      }
      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      if (!chat.getUsers().contains(user)) {
        throw new ResourceNotFoundException("User is not present in the group chat");
      }

      if (chat.getGroupAdmins().contains(user)) {
        throw new GroupChatOperationException("User is already an admin");
      }

      chat.getGroupAdmins().add(user);
      chatRepository.save(chat);
      cacheManager.getCache("allchats").evict(adminUuid.toString());
      cacheManager.getCache("allchats").put(adminUuid, getChats(adminUuid));

      cacheManager.getCache("groupChatInfo").evict(chatId.toString() + "-" + adminUuid.toString());
      cacheManager.getCache("groupChatInfo").put(chatId.toString() + "-" + adminUuid.toString(), chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupChatOperationException("Error adding as admin: " + e.getMessage());
    }
  }

  /**
   * Removes a user as an admin in a group chat.
   *
   * @param chatId    the ID of the chat to remove the user as admin
   * @param userId    the ID of the user to remove as admin
   * @param adminUuid the ID of the admin user performing the operation
   * @return true if the user was removed as admin successfully, false otherwise
   */
  @Override
  public Boolean removeAsAdmin(UUID chatId, UUID userId, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new NotGroupChatException("This is not a group chat");
      }
      if (!chat.getGroupAdmins().stream().anyMatch(admin -> admin.getId().equals(adminUuid))) {
        return false; // Not an admin
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      if (!chat.getUsers().contains(user)) {
        throw new ResourceNotFoundException("User is not present in the group chat");
      }

      if (!chat.getGroupAdmins().contains(user)) {
        throw new GroupChatOperationException("User is not an admin");
      }

      chat.getGroupAdmins().removeIf(admin -> admin.getId().equals(userId));
      chatRepository.save(chat);
      cacheManager.getCache("allchats").evict(adminUuid.toString());
      cacheManager.getCache("allchats").put(adminUuid, getChats(adminUuid));

      cacheManager.getCache("groupChatInfo").evict(chatId.toString() + "-" + adminUuid.toString());
      cacheManager.getCache("groupChatInfo").put(chatId.toString() + "-" + adminUuid.toString(), chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupChatOperationException("Error removing as admin: " + e.getMessage());
    }
  }

  /**
   * Leaves a group chat.
   *
   * @param chatId the ID of the chat to leave
   * @param userId the ID of the user leaving the chat
   * @return true if the user left the chat successfully, false otherwise
   */
  @Override
  public Boolean leaveGroupChat(UUID chatId, UUID userId) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new NotGroupChatException("This is not a group chat");
      }
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      if (!chat.getUsers().contains(user)) {
        throw new ResourceNotFoundException("User is not present in the group chat");
      }

      chat.getUsers().remove(user);
      chat.getGroupAdmins().removeIf(admin -> admin.getId().equals(userId));
      chatRepository.save(chat);
      cacheManager.getCache("allchats").evict(userId.toString());
      cacheManager.getCache("allchats").put(userId, getChats(userId));

      cacheManager.getCache("groupChatInfo").evict(chatId.toString() + "-" + userId.toString());
      cacheManager.getCache("groupChatInfo").put(chatId.toString() + "-" + userId.toString(), chat);

      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupChatOperationException("Error leaving group chat: " + e.getMessage());
    }
  }

  /**
   * Gets information about a group chat.
   *
   * @param chatId the ID of the chat to get information about
   * @param userId the ID of the user requesting the information
   * @return a Chats object containing chat details
   */
  @Override
  @Cacheable(value = "groupChatInfo", key = "#chatId + '-' + #userId")
  public Chats getGroupChatInfo(UUID chatId, UUID userId) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new NotGroupChatException("This is not a group chat");
      }
      if (!chat.getUsers().stream().anyMatch(user -> user.getId().equals(userId))) {
        throw new ResourceNotFoundException("User is not present in the group chat");
      }

      return chat;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupChatOperationException("Error fetching group chat info: " + e.getMessage());
    }
  }

  /**
   * Updates the description of a group chat.
   *
   * @param chatId      the ID of the chat to update
   * @param description the new description for the chat
   * @param adminUuid   the ID of the admin user performing the update
   * @return true if the update was successful, false otherwise
   */
  @Override
  public Boolean updateGroupChatDescription(UUID chatId, String description, UUID adminUuid) {
    try {
      Chats chat = chatRepository.findById(chatId)
          .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

      if (!chat.getIsGroupChat()) {
        throw new NotGroupChatException("This is not a group chat");
      }
      chat.setChatDescription(description);
      chatRepository.save(chat);
      cacheManager.getCache("allchats").evict(adminUuid.toString());
      cacheManager.getCache("allchats").put(adminUuid, getChats(adminUuid));

      cacheManager.getCache("groupChatInfo").evict(chatId.toString() + "-" + adminUuid.toString());
      cacheManager.getCache("groupChatInfo").put(chatId.toString() + "-" + adminUuid.toString(), chat);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      throw new GroupChatOperationException("Error updating group chat description: " + e.getMessage());
    }
  }
}
