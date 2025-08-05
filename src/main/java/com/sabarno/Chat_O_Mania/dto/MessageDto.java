package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

  /**
   * Unique identifier for the message.
   */
  private UUID id;

  /**
   * Sender of the message.
   */
  private UserDto sender;

  /**
   * Content of the message.
   */
  private String content;

  /**
   * Name of the chat to which the message belongs.
   * This field is nullable and can be used to identify the chat in which the message was sent.
   */
  private String chatName;

  /**
   * Unique identifier of the chat to which the message belongs.
   * This field is nullable and can be used to identify the chat in which the message was sent.
   */
  private UUID chatId;

  /**
   * URL of the media associated with the message.
   * This field is nullable and can be used to store the URL of any media (like images, videos, etc.) sent with the message.
   */
  private String mediaUrl;

  /**
   * Public ID of the media associated with the message.
   * This field is nullable and can be used to store the public ID of any media (like images, videos, etc.) sent with the message.
   */
  private String mediaType;
}
