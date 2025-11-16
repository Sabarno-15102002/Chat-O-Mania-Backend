package com.sabarno.chatomania.exception;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ErrorDetail {
    public String error;
    public String message;
    public LocalDateTime timestamp;
}
