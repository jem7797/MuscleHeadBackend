package com.MuscleHead.MuscleHead.Workout.SessionInstance;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastExerciseAttemptResponse {
    private LocalDate date;
    private List<LastExerciseSetResponse> sets;
}
