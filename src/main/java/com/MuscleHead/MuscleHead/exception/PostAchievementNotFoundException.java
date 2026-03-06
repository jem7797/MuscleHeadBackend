package com.MuscleHead.MuscleHead.exception;

/** Thrown when an achievement (user_medals.id) does not exist. Returns 404. */
public class PostAchievementNotFoundException extends RuntimeException {

    public PostAchievementNotFoundException(String message) {
        super(message);
    }
}
