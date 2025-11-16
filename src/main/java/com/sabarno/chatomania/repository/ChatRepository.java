package com.sabarno.chatomania.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sabarno.chatomania.entity.Chat;
import com.sabarno.chatomania.entity.User;

public interface ChatRepository extends JpaRepository<Chat, UUID>{

    @Query("SELECT c FROM Chat c JOIN c.participants u WHERE u.id = :userId")
    public List<Chat> findChatsByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM Chat c JOIN c.participants u1 JOIN c.participants u2 WHERE u1 = :user AND u2 = :reqUser AND c.isGroup = false")
    public Chat findSingleChatByUserIds(@Param("user") User user, @Param("reqUser") User reqUser);
}
