package com.sabarno.chatomania.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.sabarno.chatomania.service.RateLimiterService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketRateInterceptor implements ChannelInterceptor {

    @Autowired
    private RateLimiterService rateLimiter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getUser() != null) {

            String userId = accessor.getUser().getName();

            if (!rateLimiter.allow("ws:" + userId)) {
                log.warn(
                        "WS_RATE_LIMIT user={} destination={} session={}",
                        userId,
                        accessor.getDestination(),
                        accessor.getSessionId());
                throw new MessagingException("Rate limit exceeded");
            }
        }
        return message;
    }
}
