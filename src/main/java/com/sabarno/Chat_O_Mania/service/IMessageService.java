package com.sabarno.Chat_O_Mania.service;

import java.util.List;
import java.util.UUID;

import com.sabarno.Chat_O_Mania.dto.DeleteMessageRequestDto;
import com.sabarno.Chat_O_Mania.dto.EditMessageRequestDto;
import com.sabarno.Chat_O_Mania.dto.MessageDto;
import com.sabarno.Chat_O_Mania.dto.SendMessageRequestDto;

public interface IMessageService {

  List<MessageDto> getAllMessages(UUID chatId);
  MessageDto sendMessage(UUID senderId, SendMessageRequestDto request);
  List<UUID> getRecipientsInChat(UUID chatId);
  MessageDto editMessage(UUID senderId, EditMessageRequestDto request);
  boolean deleteMessage(UUID senderId, DeleteMessageRequestDto request);
}
