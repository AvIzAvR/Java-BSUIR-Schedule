package com.java.labs.avi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "This should be application specific";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bodyOfResponse);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleServerError(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "An internal error occurred";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(bodyOfResponse);
    }
}
