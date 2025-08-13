package com.sabarno.Chat_O_Mania.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sabarno.Chat_O_Mania.entity.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    // List<Message> findByChatId(UUID chatId);

    Page<Message> findByChatId(UUID chatId, Pageable pageable);

    @Modifying
    @Query("UPDATE Message m SET m.deliveredAt = :deliveredAt WHERE m.id = :messageId AND m.receiverId = :receiverId")
    void markDelivered(@Param("receiverId") String receiverId, @Param("messageId") String messageId,
            @Param("deliveredAt") Instant deliveredAt);

    // Search by keyword within a chat
    @Query("SELECT m FROM Message m " +
            "WHERE m.chat.id = :chatId " +
            "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND m.isDeleted = false")
    List<Message> searchMessagesByKeyword(
            @Param("chatId") UUID chatId,
            @Param("keyword") String keyword);

    // Search by date range within a chat
    @Query("SELECT m FROM Message m " +
            "WHERE m.chat.id = :chatId " +
            "AND m.createdAt BETWEEN :startDate AND :endDate " +
            "AND m.isDeleted = false")
    List<Message> searchMessagesByDateRange(
            @Param("chatId") UUID chatId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // Search by keyword + date range
    @Query("SELECT m FROM Message m " +
            "WHERE m.chat.id = :chatId " +
            "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND m.createdAt BETWEEN :startDate AND :endDate " +
            "AND m.isDeleted = false")
    List<Message> searchMessagesByKeywordAndDate(
            @Param("chatId") UUID chatId,
            @Param("keyword") String keyword,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}
