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
  private UUID userId;
  private String username;
  private String email;
  private String mobileNumber;
  private Instant lastSeen;
  private String profilePicUrl;
  private String profilePicPublicId;
  private String bio;
}
