package com.sabarno.Chat_O_Mania.dto;

import java.util.List;
import java.util.UUID;

import com.sabarno.Chat_O_Mania.entity.Message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FetchChatDto {

  private UUID id;
  private String chatName;
  private Boolean isGroupChat = false;
  private List<UserDto> users;
  private List<Message> latestMessages;
  private List<UserDto> groupAdmins ;
}
