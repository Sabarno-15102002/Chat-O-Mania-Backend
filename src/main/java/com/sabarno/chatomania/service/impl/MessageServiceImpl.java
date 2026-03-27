package com.sabarno.chatomania.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.sabarno.chatomania.entity.Chat;
import com.sabarno.chatomania.entity.Media;
import com.sabarno.chatomania.entity.Message;
import com.sabarno.chatomania.entity.MessageReaction;
import com.sabarno.chatomania.entity.Notification;
import com.sabarno.chatomania.entity.PinnedMessage;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.BadRequestException;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.MessageException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.repository.MediaRepository;
import com.sabarno.chatomania.repository.MessageReactionRepository;
import com.sabarno.chatomania.repository.MessageRepository;
import com.sabarno.chatomania.repository.PinnedMessageRepository;
import com.sabarno.chatomania.request.DeliveredAckRequest;
import com.sabarno.chatomania.request.SendMessageRequest;
import com.sabarno.chatomania.response.CloudinaryUploadResponse;
import com.sabarno.chatomania.service.ChatService;
import com.sabarno.chatomania.service.MessageService;
import com.sabarno.chatomania.service.NotificationService;
import com.sabarno.chatomania.service.UserService;
import com.sabarno.chatomania.utility.MediaTypeResolver;
import com.sabarno.chatomania.utility.MessageEvent;
import com.sabarno.chatomania.utility.MessageState;
import com.sabarno.chatomania.utility.MessageType;
import com.sabarno.chatomania.utility.NotificationType;
import com.sabarno.chatomania.utility.ReactionType;
import com.sabarno.chatomania.utility.SeenInfo;
import com.sabarno.chatomania.utility.SeenUpdatePayload;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private MessageReactionRepository messageReactionRepository;

    @Autowired
    private PinnedMessageRepository pinnedMessageRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String folder = "Chat_O_Mania";
    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;   // 10 MB
    public static final long MAX_AUDIO_SIZE = 20 * 1024 * 1024;   // 20 MB
    public static final long MAX_DOCUMENT_SIZE = 30 * 1024 * 1024;   // 30 MB
    public static final long MAX_VIDEO_SIZE = 200 * 1024 * 1024;  // 200 MB
    
    @Override
    @Transactional
    public Message sendMessage(SendMessageRequest request) throws ChatException, UserException {

        User user = userService.findUserById(request.getUserId());
        Chat chat = chatService.findChatById(request.getChatId());

        Message message = new Message();
        message.setChat(chat);
        message.setSender(user);
        message.setTimestamp(LocalDateTime.now());
        message.setContent(request.getContent());
        message.setState(MessageState.SENT);
        CloudinaryUploadResponse uploadResponse = null;
        if (request.getFile() == null) {
            message.setType(MessageType.TEXT);
            log.info("No media file attached with the message from user {}", user.getName());
        } else {
            uploadResponse = upload(request.getFile());
            message.setType(uploadResponse.getMessageType());
            Media media = new Media();
            media.setUrl(uploadResponse.getUrl());
            media.setPublicId(uploadResponse.getPublicId());
            media.setType(uploadResponse.getMessageType());
            media.setSize(uploadResponse.getSize());
            if (uploadResponse.getMessageType() == MessageType.VIDEO) {
                media.setDuration(uploadResponse.getDuration());
                media.setThumbnailUrl(uploadResponse.getThumbnail());
            } else if (uploadResponse.getMessageType() == MessageType.AUDIO) {
                media.setDuration(uploadResponse.getDuration());
            }
            media = mediaRepository.save(media);
            message.setMedia(media);
            log.info("Media file uploaded to Cloudinary with URL: {}", uploadResponse.getUrl());
        }
        log.info("User {} is sending message to chat {}", user.getName(), chat.getChatName());
        messageRepository.save(message);

        Notification notification = new Notification();
        notification.setChatId(chat.getId());
        notification.setContent(message.getContent());
        notification.setType(NotificationType.MESSAGE);
        notification.setMessageType(message.getType());
        notification.setSenderId(message.getSender().getId());
        notification.setChatName(chat.getChatName());
        notification.setResponse(uploadResponse);
        if (chat.isGroup() == false) {
            chat.getParticipants().forEach(participant -> {
                if (!participant.getId().equals(user.getId())) {
                    notification.setReceiverId(participant.getId());
                }
                if(participant.getIsOnline() == true){
                    notificationService.sendNotificationToUser(participant.getId(), chat.getId(), notification);
                }
            });
            return message;
        }

        notificationService.sendNotificationToGroup(chat.getId(), notification);
        return message;
    }

    @Override
    public List<Message> getChatsMessages(UUID chatId, User reqUser) throws ChatException {

        Chat chat = chatService.findChatById(chatId);
        if (!chat.getParticipants().contains(reqUser)) {
            throw new ChatException("Chat is not accesible to User");
        }
        List<Message> messages = messageRepository.findByChatId(chatId);
        return messages;
    }

    @Override
    public Message findMessageById(UUID messageId) throws MessageException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message Not Found with id:" + messageId));

        return message;
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId, User reqUser) throws MessageException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message Not Found with id:" + messageId));

        if (message.getSender().getId().equals(reqUser.getId())) {
            if (message.getMedia() != null) {
                mediaRepository.deleteById(message.getMedia().getId());
                deleteMediaFromCloudinary(message.getMedia().getPublicId());
            }
            messageRepository.deleteById(messageId);

            MessageEvent event = new MessageEvent();
            event.setChatId(message.getChat().getId());
            event.setMessageId(messageId);
            event.setSenderId(reqUser.getId());
            event.setTimestamp(LocalDateTime.now());

            if (message.getChat().isGroup()) {
                notificationService.sendNotificationToGroup(messageId, event);
            } else {
                message.getChat().getParticipants().forEach(participant -> {
                    if (!participant.getId().equals(reqUser.getId()) && participant.getIsOnline() == true) {
                        notificationService.sendNotificationToUser(participant.getId(), messageId, event);
                    }
                });
            }
        }
        throw new MessageException("User cannot delete this message");
    }

    @Override
    @Transactional
    public void setMessageToSeen(UUID chatId, User reqUser) throws ChatException {
        Chat chat = chatService.findChatById(chatId);
        if (chat.isGroup()) {
            setMessageToSeenForGroup(chatId, reqUser.getId());
        } else {
            UUID recipientId = chat.getParticipants().stream()
                    .filter(participant -> !participant.getId().equals(reqUser.getId()))
                    .findFirst()
                    .get()
                    .getId();
            setMessaageToSeenForChat(chat, recipientId, reqUser);
        }
    }

    @Override
    public Message editMessage(UUID messageId, String newContent, User reqUser) throws MessageException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message Not Found with id:" + messageId));

        if (message.getSender().getId().equals(reqUser.getId())) {
            message.setContent(newContent);
            message = messageRepository.save(message);

            MessageEvent event = new MessageEvent();
            event.setChatId(message.getChat().getId());
            event.setMessageId(messageId);
            event.setSenderId(reqUser.getId());
            event.setNewContent(newContent);
            event.setTimestamp(LocalDateTime.now());

            if (message.getChat().isGroup()) {
                notificationService.sendNotificationToGroup(messageId, event);
            } else {
                message.getChat().getParticipants().forEach(participant -> {
                    if (!participant.getId().equals(reqUser.getId()) && participant.getIsOnline() == true) {
                        notificationService.sendNotificationToUser(participant.getId(), messageId, event);
                    }
                });
            }
            return message;
        }
        throw new MessageException("User cannot edit this message");
    }

    @Override
    public void acknowledgeDelivery(DeliveredAckRequest req) {

        Message msg = messageRepository.findById(req.getMessageId()).orElseThrow();

        if (msg.getDeliveredTo().stream()
              .anyMatch(d -> d.getUserId().equals(req.getUserId()))) {
            return; // idempotent
        }

        SeenInfo info = new SeenInfo();
        info.setUserId(req.getUserId());
        info.setTimestamp(LocalDateTime.now());
        msg.getDeliveredTo().add(info);

        msg.setState(MessageState.DELIVERED);

        messageRepository.save(msg);
    }

    @Override
    public void syncOfflineMessage(UUID userId) throws UserException {

        User user = userService.findUserById(userId);

        LocalDateTime lastSync =
            user.getLastSeen() == null
            ? LocalDateTime.MIN
            : user.getLastSeen();

        List<Message> messages =
            messageRepository.findMessagesForOfflineSync(userId, lastSync);

        for (Message msg : messages) {
            notificationService.syncOfflineMessage(
                userId,
                msg.getChat().getId(),
                msg);
        }

        userService.updateLastSeen(userId);
    }

    private void setMessageToSeenForGroup(UUID chatId, UUID userId) {
        List<Message> unreadMessages = messageRepository.findUnreadMessagesForUser(chatId, userId);

        if (unreadMessages.isEmpty())
            return;

        LocalDateTime now = LocalDateTime.now();

        for (Message message : unreadMessages) {
            SeenInfo info = new SeenInfo();
            info.setUserId(userId);
            info.setTimestamp(now);

            message.getSeenBy().add(info);
            message.setState(MessageState.SEEN);
        }

        messageRepository.saveAll(unreadMessages);

        // WebSocket broadcast
        SeenUpdatePayload payload = new SeenUpdatePayload();
        payload.setChatId(chatId);
        payload.setSeenByUserId(userId);
        payload.setTimestamp(now);

        notificationService.sendNotificationToGroup(chatId, payload);
    }

    private void setMessaageToSeenForChat(Chat chat, UUID recipientId, User reqUser) {

        List<Message> unreadMessages = messageRepository.findUnreadMessagesForUser(chat.getId(), reqUser.getId());

        if (unreadMessages.isEmpty())
            return;

        LocalDateTime now = LocalDateTime.now();

        for (Message message : unreadMessages) {
            SeenInfo info = new SeenInfo();
            info.setUserId(recipientId);
            info.setTimestamp(now);

            message.getSeenBy().add(info);
            message.setState(MessageState.SEEN);
        }

        messageRepository.saveAll(unreadMessages);
        Notification notification = new Notification();
        notification.setChatId(chat.getId());
        notification.setChatName(chat.getChatName());
        notification.setType(NotificationType.SEEN);
        notification.setReceiverId(recipientId);
        notification.setSenderId(reqUser.getId());

        notificationService.sendNotificationToUser(reqUser.getId(), chat.getId(), notification);
    }

    private CloudinaryUploadResponse upload(MultipartFile file) {
        try {
            validateFileSize(file);
            Map<String, Object> options = Map.of(
                "folder", folder,
                "resource_type", "auto"
            );

            Map<?, ?> result = cloudinary.uploader()
                    .upload(file.getBytes(), options);

            MessageType type = detectMessageType(result);

            Long duration = null;
            if (result.get("duration") instanceof Number d) {
                duration = d.longValue();
            }


            String thumbnailUrl = null;
            if ("video".equals(result.get("resource_type"))) {
                thumbnailUrl = cloudinary.url()
                    .resourceType("video")
                    .transformation(new Transformation()
                        .startOffset(1)
                        .crop("fill"))
                    .format("jpg")
                    .generate(result.get("public_id").toString());
            }

            return new CloudinaryUploadResponse(
                result.get("secure_url").toString(),
                result.get("public_id").toString(),
                result.get("resource_type").toString(),
                ((Number) result.get("bytes")).longValue(),
                type,
                thumbnailUrl,
                duration
            );

        } catch (Exception e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }

    private MessageType detectMessageType(Map<?, ?> result) {
        String resourceType = result.get("resource_type").toString();
        String format = result.get("format").toString();

        if ("image".equals(resourceType)) {
            return MessageType.IMAGE;
        }

        if ("video".equals(resourceType)) {
            if (List.of("mp3", "wav", "ogg", "aac").contains(format)) {
                return MessageType.AUDIO;
            }
            return MessageType.VIDEO;
        }

        return MessageType.DOCUMENT;
    }

    private void validateFileSize(MultipartFile file) throws BadRequestException {

        if (file == null || file.isEmpty()) {
            return; // text message
        }

        MessageType type = MediaTypeResolver.resolve(file);
        long size = file.getSize();

        switch (type) {
            case IMAGE -> {
                if (size > MAX_IMAGE_SIZE)
                    throw new BadRequestException("Image exceeds 10MB");
            }
            case VIDEO -> {
                if (size > MAX_VIDEO_SIZE)
                    throw new BadRequestException("Video exceeds 100MB");
            }
            case AUDIO -> {
                if (size > MAX_AUDIO_SIZE)
                    throw new BadRequestException("Audio exceeds 20MB");
            }
            case DOCUMENT -> {
                if (size > MAX_DOCUMENT_SIZE)
                    throw new BadRequestException("Document exceeds 30MB");
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        }
        
    }


    private void deleteMediaFromCloudinary(String publicId) {
        try {
            Map<String, Object> options = Map.of(
                "resource_type", "auto"
            );
            cloudinary.uploader().destroy(publicId, options);
            log.info("Media with public ID {} deleted from Cloudinary", publicId);
        } catch (Exception e) {
            log.error("Failed to delete media with public ID {} from Cloudinary: {}", publicId, e.getMessage());
        }
    }

    @Override
    public void toggleReaction(UUID messageId, UUID userId, Integer reactionType) throws MessageException, UserException, BadRequestException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message Not Found with id:" + messageId));

        User user = userService.findUserById(userId);

        if (reactionType == null || reactionType < 0 || reactionType >= ReactionType.values().length) {
            throw new BadRequestException("Invalid reaction type: " + reactionType);
        }
        ReactionType reactionEnum = ReactionType.values()[reactionType];
        
        Optional<MessageReaction> existingReaction = messageReactionRepository.findByMessageAndUserAndReactionType(message, user, reactionEnum);

        String key = "reaction:message:" + messageId + ":" + reactionEnum.name();

        if (existingReaction.isPresent()) {
            messageReactionRepository.delete(existingReaction.get());
            redisTemplate.opsForHash().delete(key, userId);
        } else {
            MessageReaction reaction = new MessageReaction();
            reaction.setMessage(message);
            reaction.setUser(user);
            reaction.setReactionType(reactionEnum);
            reaction.setCreatedAt(LocalDateTime.now());
            messageReactionRepository.save(reaction);

            redisTemplate.opsForSet().add(key,userId);
            redisTemplate.expire(key, 10, TimeUnit.MINUTES);

            Boolean groupChat = message.getChat().isGroup();

            if(groupChat) {
                notificationService.sendNotificationToGroup(message.getChat().getId(), reaction);
            } else {
                message.getChat().getParticipants().forEach(participant -> {
                    if (!participant.getId().equals(userId) && participant.getIsOnline() == true) {
                        notificationService.sendNotificationToUser(participant.getId(), message.getChat().getId(), reaction);
                    }
                });
            }
        }
    }

    @Override
    public Map<ReactionType, Long> getReactionCounts(UUID messageId) {
        Map<ReactionType, Long> result = new HashMap<>();

        for (ReactionType type : ReactionType.values()) {
            String key = "reaction:message:" + messageId + ":" + type.name();
            Long count = redisTemplate.opsForSet().size(key);
            result.put(type, count != null ? count : 0);
        }

        return result;
    }

    @Override
    public List<Message> getPinnedMessages(UUID chatId) {
        List<PinnedMessage> pinnedMessages = pinnedMessageRepository.findByChatId(chatId);
        List<Message> messages = pinnedMessages.stream()
                .map(p -> {
                    try {
                        return findMessageById(p.getMessageId());
                    } catch (MessageException e) {
                        log.error("Pinned message with id {} not found: {}", p.getMessageId(), e.getMessage());
                        return null;
                    }
                })
                .filter(m -> m != null)
                .collect(Collectors.toList());
        return messages;
    }

    @Override
    public PinnedMessage pinMessage(UUID messageId, User reqUser, Long expireHour) throws ChatException, MessageException, UserException {
        Message message = findMessageById(messageId);
        Chat chat = message.getChat();

        if (!chat.getParticipants().contains(reqUser)) {
            throw new ChatException("Chat is not accesible to User");
        }

        if(chat.isGroup() && !chat.getAdmins().contains(reqUser)) {
            throw new UserException("Only admins can pin messages in a group chat");
        }

        if(pinnedMessageRepository.findByChatId(messageId).size() >= 5) {
            throw new ChatException("Maximum pinned messages limit reached for this chat");
        }

        Optional<PinnedMessage> existing =
                pinnedMessageRepository.findByChatIdAndMessageId(chat.getId(), messageId);

        if (existing.isPresent()) {
            throw new MessageException("Already pinned");
        }

        PinnedMessage pinned = new PinnedMessage();
        pinned.setChatId(chat.getId());
        pinned.setMessageId(messageId);
        pinned.setPinnedBy(reqUser.getId());
        pinned.setPinnedAt(LocalDateTime.now());
        pinned.setExpireHour(expireHour);

        pinnedMessageRepository.save(pinned);

        String key = "chat:" + chat.getId() + ":pinned";
        redisTemplate.opsForSet().add(key, messageId);
        redisTemplate.expire(key, expireHour, TimeUnit.HOURS);

        if(chat.isGroup()) {
            notificationService.sendNotificationToGroup(message.getChat().getId(), pinned);
        } else {
            message.getChat().getParticipants().forEach(participant -> {
                if (!participant.getId().equals(reqUser.getId()) && participant.getIsOnline() == true) {
                    notificationService.sendNotificationToUser(participant.getId(), message.getChat().getId(), pinned);
                }
            });
        }
        return pinned;
    }

    @Override
    public void unpinMessage(UUID messageId, User reqUser) throws ChatException, MessageException, UserException {
        Message message = findMessageById(messageId);
        Chat chat = message.getChat();

        if (!chat.getParticipants().contains(reqUser)) {
            throw new ChatException("Chat is not accesible to User");
        }

        if(chat.isGroup() && !chat.getAdmins().contains(reqUser)) {
            throw new UserException("Only admins can unpin messages in a group chat");
        }

        PinnedMessage pinned = pinnedMessageRepository.findByChatIdAndMessageId(chat.getId(), messageId)
                .orElseThrow(() -> new MessageException("Message is not pinned"));

        pinnedMessageRepository.delete(pinned);

        String key = "chat:" + chat.getId() + ":pinned";
        redisTemplate.opsForSet().remove(key, messageId);

        String response = "Message " + message.getContent()+ " with id " + messageId + " has been unpinned";

        if(chat.isGroup()) {
            notificationService.sendNotificationToGroup(message.getChat().getId(), response);
        } else {
            message.getChat().getParticipants().forEach(participant -> {
                if (!participant.getId().equals(reqUser.getId()) && participant.getIsOnline() == true) {
                    notificationService.sendNotificationToUser(participant.getId(), message.getChat().getId(), response);
                }
            });
        }
    }

    @Scheduled(cron = "0 0 * * * *") //runs every hour
    public void checkAndExpirePinnedMessages() {
        List<PinnedMessage> allPinned = pinnedMessageRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (PinnedMessage pinned : allPinned) {
            if (pinned.getExpireHour() != null) {
                LocalDateTime expireTime = pinned.getPinnedAt().plusHours(pinned.getExpireHour());
                if (now.isAfter(expireTime)) {
                    try {
                        unpinMessage(pinned.getMessageId(), userService.findUserById(pinned.getPinnedBy()));
                    } catch (Exception e) {
                        log.error("Failed to expire pinned message with id {}: {}", pinned.getMessageId(), e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public Message scheduleMessage(SendMessageRequest request) throws UserException, ChatException {
        User user = userService.findUserById(request.getUserId());
        Chat chat = chatService.findChatById(request.getChatId());

        Message message = new Message();
        message.setChat(chat);
        message.setSender(user);
        message.setContent(request.getContent());
        message.setState(MessageState.SCHEDULED);
        message.setScheduledTime(request.getScheduledTime());
        CloudinaryUploadResponse uploadResponse = null;
        if (request.getFile() == null) {
            message.setType(MessageType.TEXT);
            log.info("No media file attached with the message from user {}", user.getName());
        } else {
            uploadResponse = upload(request.getFile());
            message.setType(uploadResponse.getMessageType());
            Media media = new Media();
            media.setUrl(uploadResponse.getUrl());
            media.setPublicId(uploadResponse.getPublicId());
            media.setType(uploadResponse.getMessageType());
            media.setSize(uploadResponse.getSize());
            if (uploadResponse.getMessageType() == MessageType.VIDEO) {
                media.setDuration(uploadResponse.getDuration());
                media.setThumbnailUrl(uploadResponse.getThumbnail());
            } else if (uploadResponse.getMessageType() == MessageType.AUDIO) {
                media.setDuration(uploadResponse.getDuration());
            }
            media = mediaRepository.save(media);
            message.setMedia(media);
            log.info("Media file uploaded to Cloudinary with URL: {}", uploadResponse.getUrl());
        }
        log.info("User {} is sending message to chat {}", user.getName(), chat.getChatName());

        return messageRepository.save(message);
    }

    private void sendScheduledMessage(Message message) {

        Notification notification = new Notification();
        notification.setChatId(message.getChat().getId());
        notification.setContent(message.getContent());
        notification.setType(NotificationType.MESSAGE);
        notification.setMessageType(message.getType());
        notification.setSenderId(message.getSender().getId());
        notification.setChatName(message.getChat().getChatName());

        notification.setResponse(new CloudinaryUploadResponse(
            message.getMedia() != null ? message.getMedia().getUrl() : null,
            message.getMedia() != null ? message.getMedia().getPublicId() : null,
            message.getMedia() != null ? message.getMedia().getType().toString() : null,
            message.getMedia() != null ? message.getMedia().getSize() : null,
            message.getType(),
            message.getMedia() != null && message.getMedia().getThumbnailUrl() != null ? message.getMedia().getThumbnailUrl() : null,
            message.getMedia() != null && message.getMedia().getDuration() != null ? message.getMedia().getDuration() : null
        ));

        if (!message.getChat().isGroup()) {
            message.getChat().getParticipants().forEach(participant -> {
                if (!participant.getId().equals(message.getSender().getId())) {
                    notification.setReceiverId(participant.getId());
                }
                if (participant.getIsOnline()) {
                    notificationService.sendNotificationToUser(
                        participant.getId(),
                        message.getChat().getId(),
                        notification
                    );
                }
            });
        }

        notificationService.sendNotificationToGroup(
            message.getChat().getId(),
            notification
        );
    }

    private void handleFailure(Message message, Exception e) {

        int retryCount = message.getRetryCount() + 1;
        message.setRetryCount(retryCount);
        message.setLastAttemptedAt(LocalDateTime.now());

        if (retryCount >= 3) {
            // FINAL FAILURE
            message.setState(MessageState.FAILED);
        } else {
            // RETRY
            message.setState(MessageState.SCHEDULED);
            message.setScheduledTime(nextRetryTime(retryCount));
        }

        messageRepository.save(message);

        log.error("Scheduled message failed. ID: {}, Retry: {}",
                message.getId(), retryCount, e);
    }

    private LocalDateTime nextRetryTime(int retryCount) {

        long delaySeconds = (long) Math.pow(2, retryCount) * 10;
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
    @Scheduled(cron = "0 * * * * *") //runs every minute
    public void checkAndSendScheduledMessages() throws ChatException, UserException {
        List<Message> scheduledMessages = messageRepository.findByStatusAndScheduledAtBefore(MessageState.SCHEDULED, LocalDateTime.now());

        for (Message message : scheduledMessages) {
            
            if (messageRepository.lockMessage(message.getId()) == 0) {
                continue; // Another instance is processing this message
            }
            try {
                message.setLastAttemptedAt(LocalDateTime.now());
                messageRepository.save(message);
    
                sendScheduledMessage(message);
    
                message.setState(MessageState.SENT);
                messageRepository.save(message);
                
            } catch (Exception e) {
                handleFailure(message, e);
            }

        }
    }

    @Scheduled(fixedRate = 60000)
    public void recoverStuckMessages() {

        List<Message> stuck =
            messageRepository.findByStatusAndScheduledAtBefore(
                MessageState.PROCESSING,
                LocalDateTime.now().minusMinutes(5)
            );

        for (Message msg : stuck) {
            msg.setState(MessageState.SCHEDULED);
            messageRepository.save(msg);
        }
    }

}
