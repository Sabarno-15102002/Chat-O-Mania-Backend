package com.sabarno.Chat_O_Mania.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {
  /**
   * The unique identifier of the user.
   */
  private UUID userId;

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
   * The mobile number of the user.
   * This field is not nullable.
   */
  private String mobileNumber;

  /**
   * Last seen timestamp of the user.
   * This field is nullable and can be used to track the last time the user was active.
   */
  private Instant lastSeen;

  /**
   * The URL of the user's profile picture.
   * This field is nullable and can be used to store the profile picture of the user.
   */
  private String profilePicUrl;

  /**
   * The public ID of the user's profile picture.
   * This field is nullable and can be used to store the public ID of the profile picture in a cloud storage service.
   */
  private String profilePicPublicId;

  /**
   * A brief biography or description of the user.
   * This field is nullable and can be used to provide additional information about the user.
   */
  private String bio;
}
