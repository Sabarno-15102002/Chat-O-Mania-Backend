package com.sabarno.Chat_O_Mania.dto;

import lombok.Data;

@Data
public class UpdateUserDto {
  private String username;
  private String email;
  private String mobileNumber;
}
