package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresenceUpdateDto {
  /**
   * The unique identifier of the user whose presence is being updated.
   */
  private UUID userId;

  /**
   * The new online status of the user.
   * This field is not nullable.
   */
  private boolean online;
}