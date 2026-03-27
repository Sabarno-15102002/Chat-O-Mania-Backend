package com.sabarno.chatomania.request;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SendMessageRequest {

    private UUID chatId;
    private UUID userId;
    private String content;
    private MultipartFile file;
    private LocalDateTime scheduledTime;
}
