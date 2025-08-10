package com.sabarno.Chat_O_Mania.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sabarno.Chat_O_Mania.entity.Message;

public interface MessageRepository extends JpaRepository<Message, UUID>{

  List<Message> findByChatId(UUID chatId);

  @Modifying
    @Query("UPDATE Message m SET m.deliveredAt = :deliveredAt WHERE m.id = :messageId AND m.receiverId = :receiverId")
    void markDelivered(@Param("receiverId") String receiverId, @Param("messageId") String messageId, @Param("deliveredAt") Instant deliveredAt);

}
