package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SendMessageRequestDto {

  /**
   * The content of the message.
   * This field is not nullable.
   */
  private String content;

  /**
   * The unique identifier of the chat to which the message is being sent.
   * This field is not nullable.
   */
  private UUID chatId;

  /**
   * The media type of the file being sent.
   * This field is nullable and can be used to specify the type of media (like image, video, etc.) being sent with the message.
   */
  private String mediaType;

  /**
   * The file being sent with the message.
   * This field is nullable and can be used to send files (like images, videos, etc.) along with the message.
   */
  private MultipartFile file;
}
