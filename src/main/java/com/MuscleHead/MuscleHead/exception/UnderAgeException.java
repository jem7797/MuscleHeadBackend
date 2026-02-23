package com.MuscleHead.MuscleHead.exception;

/**
 * Thrown when a user under 13 attempts to sign up, or when a blocked email is used.
 */
public class UnderAgeException extends RuntimeException {

    public UnderAgeException(String message) {
        super(message);
    }
}
