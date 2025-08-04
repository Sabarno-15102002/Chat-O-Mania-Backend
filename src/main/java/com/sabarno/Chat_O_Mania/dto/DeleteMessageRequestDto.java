package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class DeleteMessageRequestDto {
  private UUID messageId;
}
