package com.sabarno.chatomania.request;

import java.util.UUID;

import lombok.Data;

@Data
public class SingleChatRequest {

    private UUID userId;
}
