package com.sabarno.Chat_O_Mania.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NotGroupChatException extends RuntimeException {
    public NotGroupChatException(String message) {
        super(message);
    }

}
