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

@RestController
@RequestMapping("/api/friends")
public class FriendsController {

  @Autowired
  private IFriendRequestService friendService;

  @PostMapping("/send")
  public ResponseEntity<FriendRequest> sendFriendRequest(Principal principal, UUID receiverId) {
    if (receiverId == null || receiverId.toString().isEmpty()) {  
      throw new IllegalArgumentException("Receiver ID cannot be null or empty");
    }
    UUID senderId = UUID.fromString(principal.getName());
    FriendRequest request =  friendService.sendFriendRequest(senderId, receiverId);
    return ResponseEntity.ok(request);
  }

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
