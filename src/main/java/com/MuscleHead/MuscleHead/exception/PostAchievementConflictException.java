package com.MuscleHead.MuscleHead.exception;

/** Thrown when user has already posted this achievement. Returns 409. */
public class PostAchievementConflictException extends RuntimeException {

    public PostAchievementConflictException(String message) {
        super(message);
    }
}
