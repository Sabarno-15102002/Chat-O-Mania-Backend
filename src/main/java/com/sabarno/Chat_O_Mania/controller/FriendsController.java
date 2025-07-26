package com.sabarno.Chat_O_Mania.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabarno.Chat_O_Mania.entity.FriendRequest;
import com.sabarno.Chat_O_Mania.service.IFriendRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/friends")
@Tag(name = "CRUD REST APIs for Friends of Chat-O-Mania", description = "APIs for managing friend requests and relationships")
public class FriendsController {

  @Autowired
  private IFriendRequestService friendService;

  @Operation(summary = "Send a friend request", description = "Sends a friend request from the authenticated user to another user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Friend request sent successfully"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid receiver ID"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PostMapping("/send")
  public ResponseEntity<FriendRequest> sendFriendRequest(Principal principal, UUID receiverId) {
    if (receiverId == null || receiverId.toString().isEmpty()) {  
      throw new IllegalArgumentException("Receiver ID cannot be null or empty");
    }
    UUID senderId = UUID.fromString(principal.getName());
    FriendRequest request =  friendService.sendFriendRequest(senderId, receiverId);
    return ResponseEntity.ok(request);
  }

  @Operation(summary = "Accept a friend request", description = "Accepts a friend request by its ID for the authenticated user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Friend request accepted successfully"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid request ID"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to accept this request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PostMapping("/accept")
  public ResponseEntity<String> acceptFriendRequest(Principal principal, UUID requestId) {
    if (requestId == null || requestId.toString().isEmpty()) {  
      throw new IllegalArgumentException("Request ID cannot be null or empty");
    }
    UUID receiverId = UUID.fromString(principal.getName());
    boolean accepted = friendService.acceptFriendRequest(requestId, receiverId);
    if(accepted){
      return ResponseEntity.ok().body("Friend request accepted");
    }
    return ResponseEntity.status(403).body("You are not authorized to accept this request");
  }

  @Operation(summary = "Reject a friend request", description = "Rejects a friend request by its ID for the authenticated user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Friend request rejected successfully"),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid request ID"),
      @ApiResponse(responseCode = "403", description = "Forbidden - User is not authorized to reject this request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @PostMapping("/reject")
  public ResponseEntity<String> rejectFriendRequest(Principal principal, UUID requestId) {
    if (requestId == null || requestId.toString().isEmpty()) {  
      throw new IllegalArgumentException("Request ID cannot be null or empty");
    }
    UUID receiverId = UUID.fromString(principal.getName());
    boolean rejected = friendService.rejectFriendRequest(requestId, receiverId);
    if(rejected){
      return ResponseEntity.ok().body("Friend request rejected");
    }
    return ResponseEntity.status(403).body("You are not authorized to reject this request");
  }

  @Operation(summary = "Get pending friend requests", description = "Fetches all pending friend requests for the authenticated user.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved pending friend requests"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @GetMapping("/pending")
  public ResponseEntity<List<FriendRequest>> getPendingRequests(Principal principal) {
    UUID userId = UUID.fromString(principal.getName());
    if (userId == null) {
      throw new IllegalArgumentException("User not authenticated");
    }
    List<FriendRequest> pendingRequests = friendService.getPendingRequests(userId);
    return ResponseEntity.ok(pendingRequests);
  }
}
