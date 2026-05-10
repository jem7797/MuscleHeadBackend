package com.MuscleHead.MuscleHead.User;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StreakEvaluationScheduler {

    private final StreakService streakService;

    public StreakEvaluationScheduler(StreakService streakService) {
        this.streakService = streakService;
    }

    @Scheduled(cron = "${user.streak.evaluation.cron:0 0 3 * * *}")
    public void evaluateStreaksDaily() {
        streakService.evaluateStreaksForActiveUsers();
    }
}
