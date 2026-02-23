package com.MuscleHead.MuscleHead.User;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MinorSignupAttemptRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "First name is required")
    private String first_name;

    @NotNull(message = "Birth date is required")
    private LocalDate birth_date;

    @NotNull(message = "Birth year is required")
    @Min(value = 1920, message = "Birth year must be 1920 or later")
    private Integer birth_year;

    @NotBlank(message = "Username is required")
    private String username;
}
