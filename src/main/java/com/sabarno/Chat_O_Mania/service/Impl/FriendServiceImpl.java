package com.sabarno.Chat_O_Mania.service.Impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sabarno.Chat_O_Mania.entity.FriendRequest;
import com.sabarno.Chat_O_Mania.entity.RequestStatus;
import com.sabarno.Chat_O_Mania.entity.User;
import com.sabarno.Chat_O_Mania.repository.FriendRequestRepository;
import com.sabarno.Chat_O_Mania.repository.UserRepository;
import com.sabarno.Chat_O_Mania.service.IFriendRequestService;

@Service
public class FriendServiceImpl implements IFriendRequestService {

  @Autowired
  private FriendRequestRepository friendRepo;

  @Autowired
  private UserRepository userRepository;

    /**
     * Sends a friend request from one user to another.
     *
     * @param senderId   the ID of the user sending the request
     * @param receiverId the ID of the user receiving the request
     * @return the created FriendRequest object
     */
  @Override
    public FriendRequest sendFriendRequest(UUID senderId, UUID receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("You cannot send friend request to yourself.");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Check if a request already exists
        Optional<FriendRequest> existingRequest = friendRepo.findBySenderAndReceiver(sender, receiver);
        if (existingRequest.isPresent()) {
            throw new RuntimeException("Friend request already sent.");
        }

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(RequestStatus.PENDING);

        friendRepo.save(request);
        return request;
    }

    /**
     * Accepts a friend request.
     *
     * @param requestId  the ID of the friend request to accept
     * @param receiverId the ID of the user accepting the request
     * @return true if the request was accepted successfully, false otherwise
     */
    @Override
    public boolean acceptFriendRequest(UUID requestId, UUID receiverId) {
        FriendRequest request = friendRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new RuntimeException("You are not authorized to accept this request");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        receiver.getFriends().add(request.getSender());
        request.getSender().getFriends().add(receiver);

        request.setStatus(RequestStatus.ACCEPTED);
        friendRepo.save(request);
        return true;
    }

    /**
     * Rejects a friend request.
     *
     * @param requestId  the ID of the friend request to reject
     * @param receiverId the ID of the user rejecting the request
     * @return true if the request was rejected successfully, false otherwise
     */
    @Override
    public boolean rejectFriendRequest(UUID requestId, UUID receiverId) {
        FriendRequest request = friendRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new RuntimeException("You are not authorized to reject this request");
        }

        request.setStatus(RequestStatus.REJECTED);
        friendRepo.save(request);
        return true;
    }

    /**
     *  Retrieves all pending friend requests for a user.
     * @param userId the ID of the user whose pending requests are to be retrieved
     * @return a list of pending FriendRequest objects
     */
    @Override
    public List<FriendRequest> getPendingRequests(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return friendRepo.findByReceiverAndStatus(user, RequestStatus.PENDING);
    }
}
