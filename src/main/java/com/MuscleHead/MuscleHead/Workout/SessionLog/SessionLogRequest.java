package com.MuscleHead.MuscleHead.Workout.SessionLog;

import java.util.List;

import com.MuscleHead.MuscleHead.Workout.SessionInstance.SessionInstanceRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SessionLogRequest {
    private String notes;

    private Long routineId;

    private Integer timeSpentInGym;

    @NotNull(message = "Exercises list is required")
    @NotEmpty(message = "Exercises list cannot be empty")
    @Valid
    private List<SessionInstanceRequest> exercises;
}
