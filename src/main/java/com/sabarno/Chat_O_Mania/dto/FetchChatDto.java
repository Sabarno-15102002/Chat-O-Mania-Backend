package com.sabarno.Chat_O_Mania.dto;

import java.util.List;
import java.util.UUID;

import com.sabarno.Chat_O_Mania.entity.Message;

import lombok.Data;

@Data
public class FetchChatDto {

  /**
   * Unique identifier for the chat.
   */
  private UUID id;

  /**
   * Name of the chat.
   */
  private String chatName;

  /**
   * Indicates whether the chat is a group chat.
   */
  private Boolean isGroupChat = false;

  /**
   * List of users in the chat.
   * This field is a many-to-many relationship with the User entity.
   */
  private List<UserDto> users;

  /**
   * List of latest messages in the chat.
   * This field is a one-to-many relationship with the Message entity.
   */
  private List<Message> latestMessages;

  /**
   * List of group admins in the chat.
   * This field is a many-to-many relationship with the User entity.
   */
  private List<UserDto> groupAdmins ;
}
