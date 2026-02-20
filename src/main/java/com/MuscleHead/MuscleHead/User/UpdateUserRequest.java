package com.MuscleHead.MuscleHead.User;

import java.util.Map;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * DTO for partial user updates (PATCH). All fields are optional.
 * Only non-null fields are applied to the existing user.
 */
@Data
public class UpdateUserRequest {

    private String username;

    @Email(message = "Email must be a valid email address")
    private String email;
    private String first_name;
    private Integer height;
    private Integer weight;
    private Boolean show_weight;
    private Boolean show_height;
    private Boolean stat_tracking;
    private String privacy_setting;
    private String profilePicUrl;
    private Boolean nattyStatus;
    private String bio;
    private Map<String, String> workoutSchedule;
}
