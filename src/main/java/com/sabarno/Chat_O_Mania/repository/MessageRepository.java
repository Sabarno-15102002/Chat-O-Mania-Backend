package com.sabarno.Chat_O_Mania.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabarno.Chat_O_Mania.entity.Message;

public interface MessageRepository extends JpaRepository<Message, UUID>{

  List<Message> findByChatId(UUID chatId);

}
