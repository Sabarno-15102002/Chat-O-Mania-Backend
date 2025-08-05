package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID; 

import lombok.Data;

@Data
public class EditMessageRequestDto {
  /**
   * The unique identifier of the message to be edited.
   */
  private UUID messageId;

  /**
   * The new content of the message.
   */
  private String newContent;
}
