package com.sabarno.chatomania.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sabarno.chatomania.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService{

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotificationToUser(UUID senderId, UUID chatId, Object payload) {
        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/user/"+chatId.toString(),
                payload
        );
    }

    @Override
    public void sendNotificationToGroup(UUID chatId, Object payload) {
        messagingTemplate.convertAndSend(
            "/group/"+chatId.toString(),
            payload
        );
    }

}
