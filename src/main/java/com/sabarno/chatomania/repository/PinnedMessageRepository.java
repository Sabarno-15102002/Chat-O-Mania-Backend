package com.sabarno.chatomania.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabarno.chatomania.entity.PinnedMessage;

public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, UUID> {

    List<PinnedMessage> findByChatId(UUID chatId);

    Optional<PinnedMessage> findByChatIdAndMessageId(UUID chatId, UUID messageId);

    void deleteByChatIdAndMessageId(UUID chatId, UUID messageId);
}