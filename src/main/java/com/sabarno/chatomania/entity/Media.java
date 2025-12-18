package com.sabarno.chatomania.entity;

import java.util.UUID;

import com.sabarno.chatomania.utility.MessageType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String url;
    private String publicId;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private Long size;
    private Long duration;

    private String thumbnailUrl;
}
