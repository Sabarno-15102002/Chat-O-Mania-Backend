package com.sabarno.chatomania.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabarno.chatomania.entity.Chat;
import com.sabarno.chatomania.entity.Message;
import com.sabarno.chatomania.entity.Notification;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.MessageException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.repository.MessageRepository;
import com.sabarno.chatomania.request.SendMessageRequest;
import com.sabarno.chatomania.service.ChatService;
import com.sabarno.chatomania.service.MessageService;
import com.sabarno.chatomania.service.NotificationService;
import com.sabarno.chatomania.service.UserService;
import com.sabarno.chatomania.utility.MessageEvent;
import com.sabarno.chatomania.utility.MessageState;
import com.sabarno.chatomania.utility.MessageType;
import com.sabarno.chatomania.utility.NotificationType;
import com.sabarno.chatomania.utility.SeenInfo;
import com.sabarno.chatomania.utility.SeenUpdatePayload;

@Service
public class MessageServiceImpl implements MessageService{

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Message sendMessage(SendMessageRequest request) throws ChatException, UserException {
        
        User user = userService.findUserById(request.getUserId());
        Chat chat = chatService.findChatById(request.getChatId());
        
        Message message = new Message();
        message.setChat(chat);
        message.setSender(user);
        message.setTimestamp(LocalDateTime.now());
        message.setContent(request.getContent());
        message.setState(MessageState.SENT);
        message.setType(MessageType.TEXT);
        messageRepository.save(message);

        Notification notification = new Notification();
        notification.setChatId(chat.getId());
        notification.setContent(message.getContent());
        notification.setType(NotificationType.MESSAGE);
        notification.setMessageType(MessageType.TEXT);
        notification.setSenderId(message.getSender().getId());
        notification.setChatName(chat.getChatName());
        if(chat.isGroup() == false){
            chat.getParticipants().forEach(participant -> {
                if(!participant.getId().equals(user.getId())){
                    notification.setReceiverId(participant.getId());
                }
            });
            notificationService.sendNotificationToUser(user.getId(), chat.getId(), notification);
            return message;
        }

        notificationService.sendNotificationToGroup(chat.getId(), notification);
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

            MessageEvent event = new MessageEvent();
            event.setChatId(message.getChat().getId());
            event.setMessageId(messageId);
            event.setSenderId(reqUser.getId());
            event.setTimestamp(LocalDateTime.now());

            if(message.getChat().isGroup()){
                notificationService.sendNotificationToGroup(messageId, event);
            }
            else{
                notificationService.sendNotificationToUser(reqUser.getId(), messageId, event);
            }
        }
        throw new MessageException("User cannot delete this message");
    }

    @Override
    @Transactional
    public void setMessageToSeen(UUID chatId, User reqUser) throws ChatException {
        Chat chat = chatService.findChatById(chatId);
        if(chat.isGroup()){
            setMessageToSeenForGroup(chatId, reqUser.getId());
        }
        else{
            UUID recipientId = chat.getParticipants().stream()
                .filter(participant -> !participant.getId().equals(reqUser.getId()))
                .findFirst()
                .get()
                .getId();
            setMessaageToSeenForChat(chat, recipientId, reqUser);
        }
    }

    private void setMessageToSeenForGroup(UUID chatId, UUID userId) {
        List<Message> unreadMessages =
                messageRepository.findUnreadMessagesForUser(chatId, userId);

        if (unreadMessages.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        for (Message message : unreadMessages) {
            SeenInfo info = new SeenInfo();
            info.setUserId(userId);
            info.setTimestamp(now);

            message.getSeenBy().add(info);
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
        
        messageRepository.setMessagesToSeenByChatId(chat.getId(), MessageState.SEEN);
        Notification notification = new Notification();
        notification.setChatId(chat.getId());
        notification.setChatName(chat.getChatName());
        notification.setType(NotificationType.SEEN);
        notification.setReceiverId(recipientId);
        notification.setSenderId(reqUser.getId());

        notificationService.sendNotificationToUser(reqUser.getId(), chat.getId(), notification);
    }

    @Override
    public Message editMessage(UUID messageId, String newContent, User reqUser) throws MessageException {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new MessageException("Message Not Found with id:" + messageId));

        if(message.getSender().getId().equals(reqUser.getId())){
            message.setContent(newContent);
            message = messageRepository.save(message);
            
            MessageEvent event = new MessageEvent();
            event.setChatId(message.getChat().getId());
            event.setMessageId(messageId);
            event.setSenderId(reqUser.getId());
            event.setNewContent(newContent);
            event.setTimestamp(LocalDateTime.now());

            if(message.getChat().isGroup()){
                notificationService.sendNotificationToGroup(messageId, event);
            }
            else{
                notificationService.sendNotificationToUser(reqUser.getId(), messageId, event);
            }
            return message;
        }
        throw new MessageException("User cannot edit this message");
    }
}
