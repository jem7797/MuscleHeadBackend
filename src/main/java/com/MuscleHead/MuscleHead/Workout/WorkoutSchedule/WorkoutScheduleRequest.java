package com.MuscleHead.MuscleHead.Workout.WorkoutSchedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkoutScheduleRequest {

    @NotNull(message = "Day of week is required")
    @Min(value = 1, message = "Day must be 1-7 (Monday-Sunday)")
    @Max(value = 7, message = "Day must be 1-7 (Monday-Sunday)")
    private Integer day_of_the_week;

    private String label = "";
}
