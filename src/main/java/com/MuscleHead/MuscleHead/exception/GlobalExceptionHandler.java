package com.MuscleHead.MuscleHead.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles bean validation errors from @Valid annotations
     * Returns 400 Bad Request with detailed field error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        logger.warn("Validation failed: {}", fieldErrors);

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", "One or more fields failed validation");
        errorResponse.put("fieldErrors", fieldErrors);
        errorResponse.put("path", "N/A");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles validation errors (IllegalArgumentException)
     * Returns 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Illegal argument exception: {}", ex.getMessage());
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
            logger.warn("Resource conflict: {}", ex.getMessage());
            errorResponse.put("status", HttpStatus.CONFLICT.value());
            errorResponse.put("error", "Conflict");
        } else {
            logger.warn("Illegal state exception: {}", ex.getMessage());
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
            logger.warn("Resource not found: {}", ex.getMessage());
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", "Not Found");
        } else {
            logger.error("Runtime exception occurred", ex);
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
        logger.error("Unexpected exception occurred", ex);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred");
        errorResponse.put("path", "N/A");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
