package com.sabarno.chatomania.utility;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class MessageEvent {
    private UUID chatId;
    private UUID messageId;
    private UUID senderId;
    private String newContent;
    private LocalDateTime timestamp;
}
