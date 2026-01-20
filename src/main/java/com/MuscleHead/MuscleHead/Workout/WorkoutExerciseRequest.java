package com.MuscleHead.MuscleHead.Workout;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkoutExerciseRequest {
    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    @Positive(message = "Sets must be a positive number")
    private int sets;

    @Positive(message = "Reps must be a positive number")
    private int reps;

    @PositiveOrZero(message = "Weight cannot be negative")
    private double weight;

    private String notes;
}
