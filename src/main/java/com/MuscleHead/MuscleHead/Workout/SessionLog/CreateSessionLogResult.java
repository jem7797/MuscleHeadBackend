package com.MuscleHead.MuscleHead.Workout.SessionLog;

import java.util.List;

import com.MuscleHead.MuscleHead.Medal.MedalResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionLogResult {
    private SessionLog sessionLog;
    private List<MedalResponse> newlyAwardedMedals;
}
