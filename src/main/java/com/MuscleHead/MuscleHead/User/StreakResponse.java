package com.MuscleHead.MuscleHead.User;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StreakResponse {
    private int current_streak;
    private int longest_streak;
    private StreakStatus streak_status;
    private LocalDate grace_period_start;
}
