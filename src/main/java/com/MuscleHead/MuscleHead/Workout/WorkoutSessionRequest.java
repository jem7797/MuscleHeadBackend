package com.MuscleHead.MuscleHead.Workout;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkoutSessionRequest {
    private String notes;

    private Long routineId;

    @NotNull(message = "Exercises list is required")
    @NotEmpty(message = "Exercises list cannot be empty")
    @Valid
    private List<WorkoutExerciseRequest> exercises;
}
