package com.sabarno.Chat_O_Mania.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sabarno.Chat_O_Mania.entity.Chats;

@Repository
public interface ChatRepository  extends JpaRepository<Chats, UUID>{

  Optional<Chats> findByUsers_Id(UUID userId);

  @Query("SELECT c FROM Chats c JOIN c.users u1 JOIN c.users u2 WHERE c.isGroupChat = false AND u1.id = :requestingUserId AND u2.id = :targetUserId")
  List<Chats> findOneToOneChat(UUID requestingUserId, UUID targetUserId);
}
