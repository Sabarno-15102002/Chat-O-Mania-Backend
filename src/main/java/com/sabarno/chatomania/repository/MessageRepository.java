package com.sabarno.chatomania.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sabarno.chatomania.entity.Message;
import com.sabarno.chatomania.utility.MessageState;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp ASC")
    public List<Message> findByChatId(@Param("chatId") UUID chatId);

    @Query("UPDATE Message SET state = :newState WHERE chat.id = :chatId")
    @Modifying
    void setMessagesToSeenByChatId(@Param("chatId") UUID chatId, @Param("newState") MessageState state);

    @Query(" SELECT m FROM Message m WHERE m.chat.id = :chatId AND NOT EXISTS ( SELECT s FROM m.seenBy s WHERE s.userId = :userId)")
    List<Message> findUnreadMessagesForUser(UUID chatId, UUID userId);

}
