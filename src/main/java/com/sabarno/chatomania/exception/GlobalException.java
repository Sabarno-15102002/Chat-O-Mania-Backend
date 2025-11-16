package com.sabarno.chatomania.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorDetail> UserExceptionHandler(UserException e, WebRequest req){
        ErrorDetail err = new ErrorDetail();
        err.setError(e.getMessage());
        err.setMessage(req.getDescription(false));
        err.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MessageException.class)
    public ResponseEntity<ErrorDetail> MessageExceptionHandler(MessageException e, WebRequest req){
        ErrorDetail err = new ErrorDetail();
        err.setError(e.getMessage());
        err.setMessage(req.getDescription(false));
        err.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorDetail> ChatExceptionHandler(ChatException e, WebRequest req){
        ErrorDetail err = new ErrorDetail();
        err.setError(e.getMessage());
        err.setMessage(req.getDescription(false));
        err.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e, WebRequest req){
        ErrorDetail err = new ErrorDetail();
        err.setError(e.getMessage());
        err.setMessage(req.getDescription(false));
        err.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorDetail> NoHandlerFoundExceptionHandler(NoHandlerFoundException e, WebRequest req){
        ErrorDetail err = new ErrorDetail();
        err.setError(e.getMessage());
        err.setMessage(req.getDescription(false));
        err.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail> GlobalExceptionHandler(UserException e, WebRequest req){
        ErrorDetail err = new ErrorDetail();
        err.setError(e.getMessage());
        err.setMessage(req.getDescription(false));
        err.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }
}
