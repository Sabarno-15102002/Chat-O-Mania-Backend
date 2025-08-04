package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
  private UUID userId;
  private String username;
  private String email;
  private String mobileNumber;
  private Boolean isAdmin;
  private String token;
}
