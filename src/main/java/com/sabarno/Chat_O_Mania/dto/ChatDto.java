package com.sabarno.Chat_O_Mania.dto;

import java.util.List;
import java.util.UUID;

import com.sabarno.Chat_O_Mania.entity.Message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDto {

  private UUID chatId;
  private String chatName;
  private Boolean isGroupChat;
  private List<UserDto> users;
  private List<Message> latestMessages;
}
