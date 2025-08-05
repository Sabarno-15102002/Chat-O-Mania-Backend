package com.sabarno.Chat_O_Mania.dto;

import lombok.Data;

@Data
public class UpdateUserDto {

  /**
   * The unique identifier of the user.
   */
  private String username;

  /**
   * The email address of the user.
   * This field is required for user identification and communication.
   */
  private String email;

  /**
   * The mobile number of the user.
   * This field is required for user identification and communication.
   */
  private String mobileNumber;
}
