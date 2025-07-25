package com.sabarno.Chat_O_Mania.service;

import java.util.List;
import java.util.UUID;

import com.sabarno.Chat_O_Mania.dto.AccessChatRequestDto;
import com.sabarno.Chat_O_Mania.dto.ChatDto;
import com.sabarno.Chat_O_Mania.dto.CreateGroupDto;
import com.sabarno.Chat_O_Mania.dto.FetchChatDto;
import com.sabarno.Chat_O_Mania.entity.Chats;

public interface IChatsService {

  List<FetchChatDto> getChats(UUID userId);
  ChatDto accessChat(UUID requestingUserId, AccessChatRequestDto targetUserId);
  Chats createGroupChat(CreateGroupDto dto, UUID adminUuid);
  Boolean renameGroupChat(UUID chatId, String newName, UUID adminUuid);
  Boolean addToGroupChat(UUID chatId, UUID userId, UUID adminUuid);
  Boolean removeFromGroupChat(UUID chatId, UUID userId, UUID adminUuid);
  Boolean addAsAdmin(UUID chatId, UUID userId, UUID adminUuid);
  Boolean removeAsAdmin(UUID chatId, UUID userId, UUID adminUuid);
  Boolean leaveGroupChat(UUID chatId, UUID userId);
  Chats getGroupChatInfo(UUID chatId, UUID userId);
  Boolean updateGroupChatDescription(UUID chatId, String description, UUID adminUuid);
}
