package com.sabarno.Chat_O_Mania.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
  /**
   * The username of the user.
   * This field is not nullable.
   */
  private String username;

  /**
   * The email address of the user.
   * This field is not nullable.
   */
  private String email;

  /**
   * The password of the user.
   * This field is not nullable.
   */
  private String password;

  /**
   * The mobile number of the user.
   * This field is not nullable.
   */
  private String mobileNumber;

  /**
   * Indicates whether the user is an admin.
   * This field is nullable and can be used to specify if the user has administrative privileges.
   */
  private Boolean isAdmin;
}
