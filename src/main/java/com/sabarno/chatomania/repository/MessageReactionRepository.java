package com.sabarno.chatomania.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabarno.chatomania.entity.Message;
import com.sabarno.chatomania.entity.MessageReaction;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.utility.ReactionType;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, UUID> {

    Optional<MessageReaction> findByMessageAndUserAndReactionType(
            Message message, User user, ReactionType reactionType
    );

    List<MessageReaction> findByMessage(Message message);
}
