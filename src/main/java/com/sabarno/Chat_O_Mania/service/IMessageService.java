package com.sabarno.Chat_O_Mania.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.sabarno.Chat_O_Mania.dto.DeleteMessageRequestDto;
import com.sabarno.Chat_O_Mania.dto.EditMessageRequestDto;
import com.sabarno.Chat_O_Mania.dto.MessageDto;
import com.sabarno.Chat_O_Mania.dto.SendMessageRequestDto;

public interface IMessageService {

  Page<MessageDto> getMessages(UUID chatId, int page, int size);
  MessageDto sendMessage(UUID senderId, SendMessageRequestDto request);
  List<UUID> getRecipientsInChat(UUID chatId);
  MessageDto editMessage(UUID senderId, EditMessageRequestDto request);
  boolean deleteMessage(UUID senderId, DeleteMessageRequestDto request);
  void markAsDelivered(String string, String messageId);
  List<MessageDto> searchMessagesByKeywordAndDate(UUID chatId, String keyword, Instant startDate, Instant endDate);
  List<MessageDto> searchMessagesByKeyword(UUID chatId, String keyword);
  List<MessageDto> searchMessagesByDate(UUID chatId, Instant startDate, Instant endDate);
}
