package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMessageRequestDto {
  private String content;
  private UUID chatId;
  private String mediaType;
  private MultipartFile file;
}
