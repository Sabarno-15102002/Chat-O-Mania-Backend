package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class TypingStatusDto {

  /**
   * Unique identifier for the user who is typing.
   */
  private UUID from;

  /**
   * Unique identifier for the user who is receiving the typing status.
   */
  private UUID to;

  /**
   * Indicates whether the user is currently typing.
   * This field is used to notify the recipient that the sender is typing a message.
   */
  private boolean typing;
}
