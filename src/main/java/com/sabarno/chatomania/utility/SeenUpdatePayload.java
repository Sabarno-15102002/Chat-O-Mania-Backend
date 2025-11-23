package com.sabarno.chatomania.utility;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class SeenUpdatePayload {
    private UUID chatId;
    private UUID seenByUserId;
    private LocalDateTime timestamp;
}
