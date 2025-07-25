package com.sabarno.Chat_O_Mania.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sabarno.Chat_O_Mania.dto.LoginRequestDto;
import com.sabarno.Chat_O_Mania.dto.LoginResponseDto;
import com.sabarno.Chat_O_Mania.dto.ProfileDto;
import com.sabarno.Chat_O_Mania.dto.RegisterRequestDto;
import com.sabarno.Chat_O_Mania.dto.UpdatePasswordRequestDto;
import com.sabarno.Chat_O_Mania.dto.UpdateUserDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.User;
import com.sabarno.Chat_O_Mania.service.IUserService;

@RestController
@RequestMapping("/api/auth")
public class UserController {

  @Autowired
  private IUserService userService;

  @GetMapping("/users")
  public ResponseEntity<List<User>> allUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(@RequestBody RegisterRequestDto user) {
    return ResponseEntity.ok(userService.registerUser(user));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto loginRequest) {
    return ResponseEntity.ok(userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword()));
  }

  @PutMapping("/update")
  public ResponseEntity<UserDto> updateUser(@RequestBody UpdateUserDto user, Principal principal) {
    if (principal == null || !principal.getName().equals(user.getUsername())) {
      return ResponseEntity.status(403).build(); // Forbidden
    }
    return ResponseEntity.ok(userService.updateUser(user, UUID.fromString(principal.getName())));
  }

  @PutMapping("/updatepassword")
  public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequestDto requestDto, Principal principal) {
    UUID userId = UUID.fromString(principal.getName());
    Boolean update = userService.updatePassword(userId, requestDto);
    if (update)
      return ResponseEntity.ok("Password updated successfully.");
    else {
      return ResponseEntity.status(400).body("Password update failed. Please check your current password.");
    }
  }

  @PutMapping("/upload-profile-picture")
  public ResponseEntity<String> uploadProfilePicture(
      @RequestBody MultipartFile file,
      Principal principal) {
    try {
      UUID userId = UUID.fromString(principal.getName());
      userService.updateProfilePicture(userId, file);
      return ResponseEntity.ok("Profile picture updated successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Failed to upload profile picture.");
    }
  }

  @GetMapping("/profile")
  public ResponseEntity<ProfileDto> getUserProfile(Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).build(); // Unauthorized
    }
    UUID userId = UUID.fromString(principal.getName());
    ProfileDto userProfile = userService.getUserProfile(userId);
    return ResponseEntity.ok(userProfile);
  }

  @PostMapping("/addbio")
  public ResponseEntity<String> addBio(@RequestBody String bio, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).build(); // Unauthorized
    }
    UUID userId = UUID.fromString(principal.getName());
    userService.addBio(userId, bio);
    return ResponseEntity.ok("Bio updated successfully.");
  }

  @GetMapping("/friends")
  public ResponseEntity<List<UserDto>> getFriends(Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).build(); // Unauthorized
    }
    UUID userId = UUID.fromString(principal.getName());
    List<UserDto> friends = userService.getFriends(userId);
    return ResponseEntity.ok(friends);
  }

  @GetMapping("/search")
  public ResponseEntity<List<UserDto>> searchUsers(String query, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).build(); // Unauthorized
    }
    UUID userId = UUID.fromString(principal.getName());
    List<UserDto> users = userService.searchUsers(query, userId);
    return ResponseEntity.ok(users);
  }

  @DeleteMapping("/removefriend")
  public ResponseEntity<String> removeFriend(UUID friendId, Principal principal) {
    if (principal == null || friendId == null) {
      return ResponseEntity.status(400).body("Invalid request parameters.");
    }
    UUID userId = UUID.fromString(principal.getName());
    List<UserDto> friends = userService.getFriends(userId);

    if (friends.stream().noneMatch(friend -> friend.getUserId().equals(friendId))) {
      return ResponseEntity.status(404).body("Friend not found.");
    }
    if (userId.equals(friendId)) {
      return ResponseEntity.status(400).body("You cannot remove yourself as a friend.");
    }
    boolean removed = userService.removeFriend(userId, friendId);
    if (removed) {
      return ResponseEntity.ok("Friend removed successfully.");
    } else {
      return ResponseEntity.status(500).body("Failed to remove friend. Please try again later.");
    }
  }
}

// Testing Done ✅

// Pending tasks:
// 1. Methods for user deletion, password reset, etc. can be added here as
// needed.
