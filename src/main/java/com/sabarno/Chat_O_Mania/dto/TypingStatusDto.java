package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class TypingStatusDto {

/**
   * Chat (group or direct) where typing is happening.
   */
  private UUID chatId;

  /**
   * Unique identifier for the user who is typing.
   */
  private UUID from;

  /**
   * Display name of the typing user.
   */
  private String username;

  /**
   * Indicates whether the user is currently typing.
   */
  private boolean typing;
}
