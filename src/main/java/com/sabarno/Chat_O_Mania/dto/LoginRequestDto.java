package com.sabarno.Chat_O_Mania.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequestDto {

  private String email;
  private String password;
}
