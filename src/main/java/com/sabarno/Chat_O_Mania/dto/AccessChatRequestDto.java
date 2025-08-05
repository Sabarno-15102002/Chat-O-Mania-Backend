package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class AccessChatRequestDto {
    /**
     * The unique identifier of the chat to be accessed.
     */
    private UUID targetUserId;
}
