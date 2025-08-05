package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

  /**
   * Unique identifier for the user.
   */
  private UUID userId;

  /**
   * The username of the user.
   */
  private String username;

  /**
   * The email address of the user.
   */
  private String email;

  /**
   * The mobile number of the user.
   */
  private String mobileNumber;

  /**
   * Indicates whether the user is an admin.
   * This field is nullable and can be used to specify if the user has administrative privileges.
   */
  private Boolean isAdmin;
}
