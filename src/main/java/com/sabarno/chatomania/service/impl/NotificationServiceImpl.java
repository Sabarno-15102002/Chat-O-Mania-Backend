package com.sabarno.chatomania.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sabarno.chatomania.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotificationToUser(UUID receiverId, UUID chatId, Object payload) {
        log.info("Sending notification {} to user: {} in chat{}", payload, receiverId, chatId);
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                chatId.toString(),
                payload);
    }

    @Override
    public void sendNotificationToGroup(UUID chatId, Object payload) {
        log.info("Sending notification {} to group: {}", payload, chatId);
        messagingTemplate.convertAndSend(
                "/group/" + chatId.toString(),
                payload);
    }

    @Override
    public void syncOfflineMessage(UUID userId, UUID chatId, Object payload) {
        log.info("Syncing offline messages {} to user: {} in chat{}", payload, userId, chatId);
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages/" + chatId.toString(),
                payload);
    }
}
