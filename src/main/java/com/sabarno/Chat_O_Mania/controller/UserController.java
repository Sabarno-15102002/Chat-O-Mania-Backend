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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "CRUD REST APIs for User Management in Chat-O-Mania", description = "APIs for managing user accounts, profiles, and authentication")
public class UserController {

  @Autowired
  private IUserService userService;

  @Operation(summary = "Get all users", description = "Fetches a list of all registered users.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved all users"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/users")
  public ResponseEntity<List<User>> allUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @Operation(summary = "Register a new user", description = "Registers a new user with the provided details.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully registered user"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data"),
      @ApiResponse(responseCode = "409", description = "Conflict - User already exists with the provided email")
  })
  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(@RequestBody RegisterRequestDto user) {
    return ResponseEntity.ok(userService.registerUser(user));
  }

  @Operation(summary = "User login", description = "Authenticates a user and returns a login response.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully logged in user"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
      @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid input data")
  })
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto loginRequest) {
    return ResponseEntity.ok(userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword()));
  }

  @Operation(summary = "Update user details", description = "Updates the details of the currently logged-in user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated user details"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to update this account"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data")
  })
  @PutMapping("/update")
  public ResponseEntity<UserDto> updateUser(@RequestBody UpdateUserDto user, Principal principal) {
    if (principal == null || !principal.getName().equals(user.getUsername())) {
      return ResponseEntity.status(403).build(); // Forbidden
    }
    return ResponseEntity.ok(userService.updateUser(user, UUID.fromString(principal.getName())));
  }

  @Operation(summary = "Update user password", description = "Updates the password of the currently logged-in user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated user password"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid current password or new password"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to update this account")
  })
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

  @Operation(summary = "Upload profile picture", description = "Uploads a new profile picture for the currently logged-in user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully uploaded profile picture"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid file format or size"),
      @ApiResponse(responseCode = "500", description = "Internal server error - Failed to upload profile picture")
  })
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

  @Operation(summary = "Get user profile", description = "Fetches the profile details of the currently logged-in user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @GetMapping("/profile")
  public ResponseEntity<ProfileDto> getUserProfile(Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).build(); // Unauthorized
    }
    UUID userId = UUID.fromString(principal.getName());
    ProfileDto userProfile = userService.getUserProfile(userId);
    return ResponseEntity.ok(userProfile);
  }

  @Operation(summary = "Add bio", description = "Updates the bio of the currently logged-in user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated bio"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PostMapping("/addbio")
  public ResponseEntity<String> addBio(@RequestBody String bio, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).build(); // Unauthorized
    }
    UUID userId = UUID.fromString(principal.getName());
    userService.addBio(userId, bio);
    return ResponseEntity.ok("Bio updated successfully.");
  }

  @Operation(summary = "Get friends list", description = "Fetches the list of friends for the currently logged-in user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved friends list"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @GetMapping("/friends")
  public ResponseEntity<List<UserDto>> getFriends(Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).build(); // Unauthorized
    }
    UUID userId = UUID.fromString(principal.getName());
    List<UserDto> friends = userService.getFriends(userId);
    return ResponseEntity.ok(friends);
  }

  @Operation(summary = "Search users", description = "Searches for users based on a query string.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users matching the search query"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid search query"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @GetMapping("/search")
  public ResponseEntity<List<UserDto>> searchUsers(String query, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).build(); // Unauthorized
    }
    UUID userId = UUID.fromString(principal.getName());
    List<UserDto> users = userService.searchUsers(query, userId);
    return ResponseEntity.ok(users);
  }

  @Operation(summary = "Remove a friend", description = "Removes a friend from the currently logged-in user's friends list.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully removed friend"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid request parameters or trying to remove self"),
      @ApiResponse(responseCode = "404", description = "Friend not found in the user's friends list"),
      @ApiResponse(responseCode = "500", description = "Internal server error - Failed to remove friend")
  })
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
