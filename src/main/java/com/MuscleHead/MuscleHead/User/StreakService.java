package com.MuscleHead.MuscleHead.User;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.Notification.NotificationService;
import com.MuscleHead.MuscleHead.Notification.NotificationType;

import jakarta.transaction.Transactional;

@Service
public class StreakService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final Clock utcClock;

    public StreakService(UserRepository userRepository, NotificationService notificationService, Clock utcClock) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.utcClock = utcClock;
    }

    @Transactional
    public StreakResponse evaluateStreak(String userId) {
        User user = getRequiredUser(userId);
        applyStreakEvaluation(user, utcToday());
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public StreakResponse onWorkoutLogged(String userId) {
        User user = getRequiredUser(userId);
        LocalDate today = utcToday();

        applyStreakEvaluation(user, today);

        if (user.getStreak_status() == StreakStatus.BROKEN || user.getCurrent_streak() <= 0) {
            user.setCurrent_streak(1);
        } else {
            user.setCurrent_streak(user.getCurrent_streak() + 1);
        }

        user.setLast_workout_date(today);
        user.setGrace_period_start(null);
        user.setStreak_status(StreakStatus.ACTIVE);
        if (user.getCurrent_streak() > user.getLongest_streak()) {
            user.setLongest_streak(user.getCurrent_streak());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void evaluateStreaksForActiveUsers() {
        List<User> users = userRepository.findByStreak_statusIn(List.of(StreakStatus.ACTIVE, StreakStatus.AT_RISK));
        LocalDate today = utcToday();

        for (User user : users) {
            StreakStatus previousStatus = user.getStreak_status();
            applyStreakEvaluation(user, today);
            userRepository.save(user);

            if (previousStatus != StreakStatus.AT_RISK && user.getStreak_status() == StreakStatus.AT_RISK) {
                notificationService.createNotification(
                        user,
                        NotificationType.STREAK_AT_RISK,
                        "Your streak is at risk \uD83D\uDD25 Log a workout to keep it alive");
            }
        }
    }

    @Transactional
    public StreakResponse getStreak(String userId) {
        User user = getRequiredUser(userId);
        return toResponse(user);
    }

    private void applyStreakEvaluation(User user, LocalDate today) {
        LocalDate lastWorkoutDate = user.getLast_workout_date();
        if (lastWorkoutDate == null) {
            user.setStreak_status(StreakStatus.BROKEN);
            user.setCurrent_streak(0);
            user.setGrace_period_start(null);
            return;
        }

        long daysSinceLastWorkout = java.time.temporal.ChronoUnit.DAYS.between(lastWorkoutDate, today);

        if (daysSinceLastWorkout <= 1) {
            user.setStreak_status(StreakStatus.ACTIVE);
            user.setGrace_period_start(null);
            return;
        }

        if (daysSinceLastWorkout <= 3) {
            user.setStreak_status(StreakStatus.AT_RISK);
            if (user.getGrace_period_start() == null) {
                user.setGrace_period_start(lastWorkoutDate.plusDays(2));
            }
            return;
        }

        user.setStreak_status(StreakStatus.BROKEN);
        user.setCurrent_streak(0);
        user.setGrace_period_start(null);
    }

    private User getRequiredUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private LocalDate utcToday() {
        return LocalDate.now(utcClock);
    }

    private StreakResponse toResponse(User user) {
        return new StreakResponse(
                user.getCurrent_streak(),
                user.getLongest_streak(),
                user.getStreak_status(),
                user.getGrace_period_start());
    }
}
