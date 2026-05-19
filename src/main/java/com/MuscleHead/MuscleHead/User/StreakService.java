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

        // Additional workouts on the same UTC day keep status fresh without bumping the streak.
        if (today.equals(user.getLastWorkoutDate())) {
            user.setGracePeriodStart(null);
            user.setStreakStatus(StreakStatus.ACTIVE);
            userRepository.save(user);
            return toResponse(user);
        }

        applyStreakEvaluation(user, today);

        if (user.getStreakStatus() == StreakStatus.BROKEN || user.getCurrentStreak() <= 0) {
            user.setCurrentStreak(1);
        } else {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        }

        user.setLastWorkoutDate(today);
        user.setGracePeriodStart(null);
        user.setStreakStatus(StreakStatus.ACTIVE);
        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void evaluateStreaksForActiveUsers() {
        List<User> users = userRepository.findByStreakStatusIn(List.of(StreakStatus.ACTIVE, StreakStatus.AT_RISK));
        LocalDate today = utcToday();

        for (User user : users) {
            StreakStatus previousStatus = user.getStreakStatus();
            applyStreakEvaluation(user, today);
            userRepository.save(user);

            if (previousStatus != StreakStatus.AT_RISK && user.getStreakStatus() == StreakStatus.AT_RISK) {
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
        applyStreakEvaluation(user, utcToday());
        userRepository.save(user);
        return toResponse(user);
    }

    private void applyStreakEvaluation(User user, LocalDate today) {
        LocalDate lastWorkoutDate = user.getLastWorkoutDate();
        if (lastWorkoutDate == null) {
            user.setStreakStatus(StreakStatus.BROKEN);
            user.setCurrentStreak(0);
            user.setGracePeriodStart(null);
            return;
        }

        long daysSinceLastWorkout = java.time.temporal.ChronoUnit.DAYS.between(lastWorkoutDate, today);

        if (daysSinceLastWorkout <= 1) {
            user.setStreakStatus(StreakStatus.ACTIVE);
            user.setGracePeriodStart(null);
            return;
        }

        if (daysSinceLastWorkout <= 3) {
            user.setStreakStatus(StreakStatus.AT_RISK);
            if (user.getGracePeriodStart() == null) {
                user.setGracePeriodStart(lastWorkoutDate.plusDays(2));
            }
            return;
        }

        user.setStreakStatus(StreakStatus.BROKEN);
        user.setCurrentStreak(0);
        user.setGracePeriodStart(null);
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
                user.getCurrentStreak(),
                user.getLongestStreak(),
                user.getStreakStatus(),
                user.getGracePeriodStart());
    }
}
