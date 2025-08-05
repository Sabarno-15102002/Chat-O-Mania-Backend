package com.sabarno.Chat_O_Mania.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {

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
   */
  private Boolean isAdmin;

  /**
   * Token for the user session.
   * This field is used for authentication and authorization.
   */
  private String token;
}
