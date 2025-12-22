package com.sabarno.chatomania.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sabarno.chatomania.utility.MessageState;
import com.sabarno.chatomania.utility.MessageType;
import com.sabarno.chatomania.utility.SeenInfo;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne
    private User sender;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @OneToOne
    @JoinColumn(name = "media_id")
    private Media media;

    @Enumerated(EnumType.STRING)
    private MessageState state;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @ElementCollection
    private Set<SeenInfo> deliveredTo = new HashSet<>();
    
    @ElementCollection
    private Set<SeenInfo> seenBy = new HashSet<>();

}
