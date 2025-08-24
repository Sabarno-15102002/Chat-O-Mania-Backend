package com.sabarno.Chat_O_Mania.mapper;

import com.sabarno.Chat_O_Mania.dto.MessageDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.Message;

public class MessageMapper {

  public static MessageDto mapToMessageDto(Message message, MessageDto messageDto, UserDto userDto) {
    messageDto.setId(message.getId());
    messageDto.setSender(userDto);
    messageDto.setContent(message.getContent());
    messageDto.setChatName(message.getChat().getChatName());
    messageDto.setChatId(message.getChat().getId());
    messageDto.setMediaUrl(message.getMediaUrl());
    messageDto.setMediaType(message.getMediaType());
    messageDto.setForwardedFromId(message.getForwardedFrom() != null ? message.getForwardedFrom().getId() : null);
    messageDto.setForwardedFromUsername(message.getForwardedFrom() != null ? message.getForwardedFrom().getSender().getUsername() : null);

    return messageDto;
  }
}
