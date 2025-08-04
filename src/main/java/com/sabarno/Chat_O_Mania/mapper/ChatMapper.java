package com.sabarno.Chat_O_Mania.mapper;

import java.util.List;

import com.sabarno.Chat_O_Mania.dto.ChatDto;
import com.sabarno.Chat_O_Mania.dto.FetchChatDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.Chats;
import com.sabarno.Chat_O_Mania.entity.Message;

public class ChatMapper {

  public static ChatDto mapToChatDto(Chats chats,ChatDto chatDto, List<UserDto> userDtos) {
    chatDto.setChatId(chats.getId());
    chatDto.setChatName(chats.getChatName());
    chatDto.setIsGroupChat(chats.getIsGroupChat());
    chatDto.setUsers(userDtos);
    chatDto.setLatestMessages(chats.getLatestMessages());
    return chatDto;
  }

  public static FetchChatDto mapToFetchChatDto(Chats chats, FetchChatDto fetchChatDto, List<UserDto> userDtos, List<Message> latestMessages,
    List<UserDto> groupAdmins) {
    fetchChatDto.setId(chats.getId());
    fetchChatDto.setChatName(chats.getChatName());
    fetchChatDto.setIsGroupChat(chats.getIsGroupChat());
    fetchChatDto.setUsers(userDtos);
    fetchChatDto.setLatestMessages(latestMessages);
    fetchChatDto.setGroupAdmins(groupAdmins);
    return fetchChatDto;
  }
}
