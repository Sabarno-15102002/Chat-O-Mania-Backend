package com.sabarno.chatomania.utility;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class SeenInfo {
    private UUID userId;
    private LocalDateTime timestamp;
}
