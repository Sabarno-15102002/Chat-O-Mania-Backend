package com.sabarno.Chat_O_Mania.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NotValidDataException extends IllegalArgumentException {
    public NotValidDataException(String message) {
        super(message);
    }

}
