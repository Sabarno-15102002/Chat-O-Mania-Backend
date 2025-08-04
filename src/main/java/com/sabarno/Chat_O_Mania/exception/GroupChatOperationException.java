package com.sabarno.Chat_O_Mania.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class GroupChatOperationException extends RuntimeException {

  public GroupChatOperationException(String message) {
    super(message);
  }

}
