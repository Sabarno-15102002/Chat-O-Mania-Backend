package com.sabarno.Chat_O_Mania.dto;

import lombok.Data;

@Data
public class LoginRequestDto {

  /**
   * The email address of the user.
   */
  private String email;

  /**
   * The password of the user.
   * This field is not nullable.
   */
  private String password;
}
