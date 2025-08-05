package com.sabarno.Chat_O_Mania.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest extends BaseEntity {
    /**
     * Unique identifier for the friend request.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The user who sent the friend request.
     */
    @ManyToOne
    private User sender;

    /**
     * The user who received the friend request.
     */
    @ManyToOne
    private User receiver;

    /**
     * The status of the friend request.
     */
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

}

