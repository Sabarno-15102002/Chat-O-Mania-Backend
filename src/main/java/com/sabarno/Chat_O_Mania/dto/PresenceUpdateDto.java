package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresenceUpdateDto {
  private UUID userId;
  private boolean online;
}