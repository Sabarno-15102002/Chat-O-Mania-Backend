package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {

  private UUID userId;
  private String username;
  private String email;
  private String mobileNumber;
  private Boolean isAdmin;
}
