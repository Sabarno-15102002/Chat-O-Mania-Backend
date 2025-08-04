package com.sabarno.Chat_O_Mania.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
  private String username;
  private String email;
  private String password;
  private String mobileNumber;
  private Boolean isAdmin;
}
