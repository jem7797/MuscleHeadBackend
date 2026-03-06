package com.MuscleHead.MuscleHead.exception;

/** Thrown when user doesn't own the achievement or userId doesn't match auth. Returns 403. */
public class PostAchievementForbiddenException extends RuntimeException {

    public PostAchievementForbiddenException(String message) {
        super(message);
    }
}
