package com.sabarno.chatomania.request;

import java.util.UUID;

import lombok.Data;

@Data
public class DeliveredAckRequest {
    private UUID messageId;
    private UUID userId;
}
