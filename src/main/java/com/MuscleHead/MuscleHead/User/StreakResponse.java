package com.MuscleHead.MuscleHead.User;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StreakResponse {
    private int currentStreak;
    private int longestStreak;
    private StreakStatus streakStatus;
    private LocalDate gracePeriodStart;
}
