package com.sabarno.chatomania.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class PinnedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID chatId;
    private UUID messageId;
    private UUID pinnedBy;
    private LocalDateTime pinnedAt;
    private Long expireHour;
}