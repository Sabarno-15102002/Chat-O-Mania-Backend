package com.sabarno.chatomania.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sabarno.chatomania.entity.Message;
import com.sabarno.chatomania.entity.PinnedMessage;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.BadRequestException;
import com.sabarno.chatomania.exception.ChatException;
import com.sabarno.chatomania.exception.MessageException;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.request.DeliveredAckRequest;
import com.sabarno.chatomania.request.SendMessageRequest;
import com.sabarno.chatomania.utility.ReactionType;

public interface MessageService {

    public Message sendMessage(SendMessageRequest request) throws ChatException, UserException;
    public List<Message> getChatsMessages(UUID chatId, User reqUser) throws ChatException;
    public Message findMessageById(UUID messageId) throws MessageException;
    public Message editMessage(UUID messageId, String newContent, User reqUser) throws MessageException;
    public void deleteMessage(UUID messageId, User reqUser) throws MessageException;
    public void setMessageToSeen(UUID chatId, User reqUser) throws ChatException;
    public void syncOfflineMessage(UUID userId) throws UserException;
    public void acknowledgeDelivery(DeliveredAckRequest req);
    public void toggleReaction(UUID messageId, UUID userId, Integer reactionType) throws MessageException, UserException, BadRequestException;
    public Map<ReactionType, Long> getReactionCounts(UUID messageId) throws MessageException;
    public PinnedMessage pinMessage(UUID messageId, User reqUser, Long expireHour) throws ChatException, MessageException, UserException;
    public void unpinMessage(UUID messageId, User reqUser) throws ChatException, MessageException, UserException;
    public List<Message> getPinnedMessages(UUID chatId);
}
