package com.sabarno.Chat_O_Mania.service.Impl;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.sabarno.Chat_O_Mania.dto.DeleteMessageRequestDto;
import com.sabarno.Chat_O_Mania.dto.EditMessageRequestDto;
import com.sabarno.Chat_O_Mania.dto.MessageDto;
import com.sabarno.Chat_O_Mania.dto.SendMessageRequestDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.Chats;
import com.sabarno.Chat_O_Mania.entity.Message;
import com.sabarno.Chat_O_Mania.entity.User;
import com.sabarno.Chat_O_Mania.exception.GroupChatOperationException;
import com.sabarno.Chat_O_Mania.exception.NotValidDataException;
import com.sabarno.Chat_O_Mania.exception.ResourceNotFoundException;
import com.sabarno.Chat_O_Mania.mapper.MessageMapper;
import com.sabarno.Chat_O_Mania.mapper.UserMapper;
import com.sabarno.Chat_O_Mania.repository.ChatRepository;
import com.sabarno.Chat_O_Mania.repository.MessageRepository;
import com.sabarno.Chat_O_Mania.repository.UserRepository;
import com.sabarno.Chat_O_Mania.service.IMessageService;
import com.sabarno.Chat_O_Mania.service.PresenceService;

@Service
public class MessageServiceImpl implements IMessageService {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChatRepository chatRepository;

  @Autowired
  private Cloudinary cloudinary;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  private PresenceService presenceService;

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  /**
   * Retrieves all messages for a given chat.
   *
   * @param chatId the ID of the chat for which to retrieve messages
   * @return a list of MessageDto objects representing the messages in the chat
   */
  @Override
  public List<MessageDto> getAllMessages(UUID chatId) {
    try {
      List<Message> messages = messageRepository.findByChatId(chatId);

      List<MessageDto> messageList = messages.stream().map(message -> MessageMapper.mapToMessageDto(message,
          new MessageDto(), UserMapper.mapToUserDto(message.getSender(), new UserDto()))).collect(Collectors.toList());
      return messageList;
    } catch (Exception e) {
      throw new GroupChatOperationException("Error retrieving messages for chat: " + e);
    }
  }

  /**
   * Sends a message in a chat.
   *
   * @param senderId the ID of the user sending the message
   * @param request  the request containing chat ID, content, and optional file
   * @return the sent MessageDto object
   */
  @Override
  public MessageDto sendMessage(UUID senderId, SendMessageRequestDto request) {
    // 1. Validate
    if (request.getChatId() == null
        || (request.getContent() == null && (request.getFile() == null || request.getFile().isEmpty()))) {
      throw new NotValidDataException("Message must have content or media");
    }

    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

    Chats chat = chatRepository.findById(request.getChatId())
        .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

    Message message = new Message();
    message.setSender(sender);
    message.setChat(chat);

    message.setContent(request.getContent());

    if (request.getFile() != null && !request.getFile().isEmpty()) {
      try {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
            request.getFile().getBytes(),
            Map.of("upload_preset", "ChatOManiaMedia"));

        String mediaUrl = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();

        message.setMediaUrl(mediaUrl);
        message.setMediaPublicId(publicId);
        message.setMediaType(request.getMediaType());

      } catch (IOException e) {
        throw new GroupChatOperationException("Media upload failed" + e.getMessage());
      }
    }

    message = messageRepository.save(message);
    chat.getLatestMessages().add(message);
    chatRepository.save(chat);

    message.setChat(chat);

    MessageDto messageDto = MessageMapper.mapToMessageDto(message, new MessageDto(), UserMapper.mapToUserDto(sender, new UserDto()));

    // Resolve recipients (works for group chats too)
    List<UUID> recipients = getRecipientsInChat(chat.getId());
    recipients.remove(senderId); // Remove sender from recipients

    for (UUID recipient : recipients) {
        boolean isOnline = presenceService.isUserOnline(recipient.toString());

        if (isOnline) {
            messagingTemplate.convertAndSend(
                "/topic/chat/" + chat.getId() + "/messages", messageDto
            );
        } else {
            // Queue in Redis for later delivery
            String queueKey = "pending:" + recipient.toString();
            redisTemplate.opsForList().rightPush(queueKey, messageDto);
        }
    }

    return messageDto;
  }

  /**
   * Retrieves the list of recipients in a chat.
   *
   * @param chatId the ID of the chat for which to retrieve recipients
   * @return a list of UUIDs representing the IDs of users in the chat
   */
  @Override
  public List<UUID> getRecipientsInChat(UUID chatId) {
    Chats chat = chatRepository.findById(chatId)
        .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

    return chat.getUsers().stream()
        .map(User::getId)
        .collect(Collectors.toList());
  }

  /**
   * Edits a message in a chat.
   *
   * @param senderId the ID of the user editing the message
   * @param request  the request containing message ID and new content
   * @return the edited MessageDto object
   */
  @Override
  public MessageDto editMessage(UUID senderId, EditMessageRequestDto request) {
    Message message = messageRepository.findById(request.getMessageId())
        .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

    boolean isSender = message.getSender().getId().equals(senderId);
    // Allow only sender to edit within 5 minutes
    LocalDateTime messageTime = (message.getUpdatedAt() == null) ? message.getCreatedAt() : message.getUpdatedAt();
    boolean withinLimit = Duration.between(messageTime, Instant.now()).toMinutes() <= 5;

    if (isSender && withinLimit) {
      message.setContent(request.getNewContent());
      message.setEdited(true);
      message.setUpdatedAt(LocalDateTime.now());
      messageRepository.save(message);
      notifyClientsOfEdit(message);
      return MessageMapper.mapToMessageDto(message, new MessageDto(),
          UserMapper.mapToUserDto(message.getSender(), new UserDto()));
    }
    throw new GroupChatOperationException("You can only edit your own messages within 5 minutes of sending.");
  }

  /**
   * Deletes a message in a chat.
   *
   * @param senderId the ID of the user deleting the message
   * @param request  the request containing message ID
   * @return true if the message was deleted, false otherwise
   */
  @Override
  public boolean deleteMessage(UUID senderId, DeleteMessageRequestDto request) {
    Optional<Message> optional = messageRepository.findById(request.getMessageId());
    if (optional.isEmpty())
      return false;

    Message message = optional.get();
    Chats chat = message.getChat();
    boolean isGroup = chat.getIsGroupChat();

    boolean isSender = message.getSender().getId().equals(senderId);
    boolean isAdmin = isGroup && chat.getGroupAdmins().stream()
        .anyMatch(admin -> admin.getId().equals(senderId));
    LocalDateTime messageTime = (message.getUpdatedAt() == null) ? message.getCreatedAt() : message.getUpdatedAt();
    boolean withinLimit = Duration.between(messageTime, Instant.now()).toMinutes() <= 5;

    if ((isSender && withinLimit) || (isGroup && isAdmin)) {
      messageRepository.delete(message);

      List<UUID> recipients = getRecipientsInChat(chat.getId());
      for (UUID userId : recipients) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/messages/delete",
            request.getMessageId());
      }
      return true;
    }
    return false;
  }

  /**
   * Notifies clients of an edited message.
   *
   * @param message the edited message to notify clients about
   */
  private void notifyClientsOfEdit(Message message) {

    List<UUID> recipients = getRecipientsInChat(message.getChat().getId());

    for (UUID recipientId : recipients) {
      messagingTemplate.convertAndSendToUser(
          recipientId.toString(),
          "/queue/messages/edited",
          MessageMapper.mapToMessageDto(message, new MessageDto(),
              UserMapper.mapToUserDto(message.getSender(), new UserDto())));
    }
  }
}
