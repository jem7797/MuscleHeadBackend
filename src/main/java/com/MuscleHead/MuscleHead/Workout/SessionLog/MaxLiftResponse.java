package com.MuscleHead.MuscleHead.Workout.SessionLog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaxLiftResponse {
    private long sessionId;
    private double maxLift;
}
