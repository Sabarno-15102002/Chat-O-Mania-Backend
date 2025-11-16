package com.sabarno.chatomania.request;

import java.util.UUID;

import lombok.Data;

@Data
public class SendMessageRequest {

    private UUID chatId;
    private UUID userId;
    private String content;
}
