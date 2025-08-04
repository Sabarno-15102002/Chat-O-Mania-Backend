package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

  private UUID id;
  private UserDto sender;
  private String content;
  private String chatName;
  private UUID chatId;
  private String mediaUrl;
  private String mediaType;
}
