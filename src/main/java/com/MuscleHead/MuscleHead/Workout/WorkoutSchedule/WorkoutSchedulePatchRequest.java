package com.MuscleHead.MuscleHead.Workout.WorkoutSchedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkoutSchedulePatchRequest {

    @Min(value = 1, message = "Day must be 1-7 (Monday-Sunday)")
    @Max(value = 7, message = "Day must be 1-7 (Monday-Sunday)")
    private Integer day_of_the_week;

    private String label;
}
