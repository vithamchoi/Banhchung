package com.quannhabaninh.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Get the first error message
        FieldError firstError = (FieldError) ex.getBindingResult().getAllErrors().get(0);
        errors.put("message", firstError.getDefaultMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
