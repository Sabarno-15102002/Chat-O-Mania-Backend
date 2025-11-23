package com.sabarno.chatomania.entity;

import java.util.UUID;

import com.sabarno.chatomania.utility.MessageType;
import com.sabarno.chatomania.utility.NotificationType;

import lombok.Data;

@Data
public class Notification {
    private UUID chatId;
    private String content;
    private UUID senderId;
    private UUID receiverId;
    private String chatName;
    private MessageType messageType;
    private NotificationType type;
    // private byte[] media;
}
