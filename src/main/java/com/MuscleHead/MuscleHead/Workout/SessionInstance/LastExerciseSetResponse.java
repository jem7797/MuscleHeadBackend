package com.MuscleHead.MuscleHead.Workout.SessionInstance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastExerciseSetResponse {
    private int reps;
    private double weight;
}
