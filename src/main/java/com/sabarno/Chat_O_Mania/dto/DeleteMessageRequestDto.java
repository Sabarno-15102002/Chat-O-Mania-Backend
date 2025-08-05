package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class DeleteMessageRequestDto {
    /**
     * The unique identifier of the message to be deleted.
     */
  private UUID messageId;
}
