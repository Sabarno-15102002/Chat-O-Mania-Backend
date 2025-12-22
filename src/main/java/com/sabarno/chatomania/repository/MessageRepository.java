package com.sabarno.chatomania.repository;

import java.time.LocalDateTime;
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
    List<Message> findByChatId(@Param("chatId") UUID chatId);

    @Query("UPDATE Message SET state = :newState WHERE chat.id = :chatId")
    @Modifying
    void setMessagesToSeenByChatId(@Param("chatId") UUID chatId, @Param("newState") MessageState state);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND NOT EXISTS ( SELECT s FROM m.seenBy s WHERE s.userId = :userId)")
    List<Message> findUnreadMessagesForUser(UUID chatId, UUID userId);

    @Query("SELECT DISTINCT m FROM Message m JOIN m.chat c JOIN c.participants p WHERE p.id = :userId AND m.sender.id <> :userId AND NOT EXISTS (SELECT d FROM m.deliveredTo d WHERE d.userId = :userId) ORDER BY m.timestamp")
    List<Message> findUndeliveredMessages(UUID userId);

    @Query("SELECT DISTINCT m FROM Message m JOIN m.chat c JOIN c.participants p WHERE p.id = :userId AND m.timestamp > :lastSync ORDER BY m.timestamp")
    List<Message> findMessagesForOfflineSync(UUID userId, LocalDateTime lastSync);

}
