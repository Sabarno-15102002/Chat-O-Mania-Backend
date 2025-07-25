package com.sabarno.Chat_O_Mania.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserDto {
  private String username;
  private String email;
  private String mobileNumber;
}
