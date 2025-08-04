package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class TypingStatusDto {
  private UUID from;
  private UUID to;
  private boolean typing;
}
