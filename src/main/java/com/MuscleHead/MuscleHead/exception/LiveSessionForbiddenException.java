package com.MuscleHead.MuscleHead.exception;

/** Thrown when user is not authorized to perform a live session action (e.g. only host can end). Returns 403. */
public class LiveSessionForbiddenException extends RuntimeException {

    public LiveSessionForbiddenException(String message) {
        super(message);
    }
}
