package com.MuscleHead.MuscleHead.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that catches exceptions across all controllers
 * and returns consistent JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors (IllegalArgumentException)
     * Returns 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", "N/A");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles business logic errors (IllegalStateException)
     * Returns 409 Conflict for duplicate resources, 400 for other state errors
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());

        // Check if it's a duplicate resource error
        if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
            errorResponse.put("status", HttpStatus.CONFLICT.value());
            errorResponse.put("error", "Conflict");
        } else {
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Bad Request");
        }

        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", "N/A");

        return ResponseEntity.status(
                ex.getMessage() != null && ex.getMessage().contains("already exists")
                        ? HttpStatus.CONFLICT
                        : HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles general runtime errors (RuntimeException)
     * Returns 404 Not Found for "not found" errors, 500 for others
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());

        // Check if it's a "not found" error
        if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", "Not Found");
        } else {
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
        }

        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", "N/A");

        return ResponseEntity.status(
                ex.getMessage() != null && ex.getMessage().contains("not found")
                        ? HttpStatus.NOT_FOUND
                        : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Catches any other unhandled exceptions
     * Returns 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred");
        errorResponse.put("path", "N/A");

        // Log the full exception for debugging (you'd use a logger here)
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
