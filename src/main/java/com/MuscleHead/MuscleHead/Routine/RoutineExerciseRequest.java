package com.MuscleHead.MuscleHead.Routine;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoutineExerciseRequest {
    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    @NotNull(message = "Order index is required")
    @PositiveOrZero(message = "Order index cannot be negative")
    private Integer orderIndex;

    @Positive(message = "Target reps must be a positive number")
    private Integer reps;

    @Positive(message = "Target sets must be a positive number")
    private Integer sets;
}
