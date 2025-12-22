package com.sabarno.chatomania.service;

import java.util.UUID;

public interface NotificationService {

    public void sendNotificationToUser(UUID senderId, UUID chatId, Object payload);
    public void sendNotificationToGroup(UUID chatId, Object payload);
    public void syncOfflineMessage(UUID userId, UUID chatId, Object payload);
}
