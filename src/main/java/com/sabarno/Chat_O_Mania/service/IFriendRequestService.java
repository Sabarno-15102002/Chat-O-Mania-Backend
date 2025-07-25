package com.sabarno.Chat_O_Mania.service;

import java.util.List;
import java.util.UUID;

import com.sabarno.Chat_O_Mania.entity.FriendRequest;

public interface IFriendRequestService {
  FriendRequest sendFriendRequest(UUID senderId, UUID receiverId);

  boolean acceptFriendRequest(UUID requestId, UUID receiverId);

  boolean rejectFriendRequest(UUID requestId, UUID receiverId);

  List<FriendRequest> getPendingRequests(UUID userId);
}
