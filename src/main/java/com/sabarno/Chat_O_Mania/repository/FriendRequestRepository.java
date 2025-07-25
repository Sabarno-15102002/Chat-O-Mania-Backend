package com.sabarno.Chat_O_Mania.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabarno.Chat_O_Mania.entity.FriendRequest;
import com.sabarno.Chat_O_Mania.entity.RequestStatus;
import com.sabarno.Chat_O_Mania.entity.User;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    List<FriendRequest> findByReceiverAndStatus(User receiver, RequestStatus status);
    List<FriendRequest> findBySenderAndStatus(User sender, RequestStatus status);
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
    Optional<List<FriendRequest>> findAllBySenderOrReceiver(User user, User user2);
}

