package com.MuscleHead.MuscleHead.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.MuscleHead.MuscleHead.Notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setup() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void logsOnDayOneTwoThree_streakIsThreeAndActive() {
        User user = createUser("user-1");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        serviceAt("2026-05-01").onWorkoutLogged("user-1");
        serviceAt("2026-05-02").onWorkoutLogged("user-1");
        StreakResponse response = serviceAt("2026-05-03").onWorkoutLogged("user-1");

        assertEquals(3, response.getCurrent_streak());
        assertEquals(3, response.getLongest_streak());
        assertEquals(StreakStatus.ACTIVE, response.getStreak_status());
        assertNull(response.getGrace_period_start());
    }

    @Test
    void missesDayFourAndFive_streakAtRiskAndIntact() {
        User user = createUser("user-2");
        user.setCurrent_streak(3);
        user.setLongest_streak(3);
        user.setLast_workout_date(LocalDate.of(2026, 5, 3));
        user.setStreak_status(StreakStatus.ACTIVE);
        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));

        StreakResponse response = serviceAt("2026-05-05").evaluateStreak("user-2");

        assertEquals(3, response.getCurrent_streak());
        assertEquals(3, response.getLongest_streak());
        assertEquals(StreakStatus.AT_RISK, response.getStreak_status());
        assertEquals(LocalDate.of(2026, 5, 5), response.getGrace_period_start());
    }

    @Test
    void logsDuringGracePeriod_streakResumesAndIncrements() {
        User user = createUser("user-3");
        user.setCurrent_streak(3);
        user.setLongest_streak(3);
        user.setLast_workout_date(LocalDate.of(2026, 5, 3));
        user.setStreak_status(StreakStatus.AT_RISK);
        user.setGrace_period_start(LocalDate.of(2026, 5, 5));
        when(userRepository.findById("user-3")).thenReturn(Optional.of(user));

        StreakResponse response = serviceAt("2026-05-06").onWorkoutLogged("user-3");

        assertEquals(4, response.getCurrent_streak());
        assertEquals(4, response.getLongest_streak());
        assertEquals(StreakStatus.ACTIVE, response.getStreak_status());
        assertNull(response.getGrace_period_start());
    }

    @Test
    void missesFourDaysOrMore_streakBreaksAndResets() {
        User user = createUser("user-4");
        user.setCurrent_streak(3);
        user.setLongest_streak(3);
        user.setLast_workout_date(LocalDate.of(2026, 5, 3));
        user.setStreak_status(StreakStatus.ACTIVE);
        when(userRepository.findById("user-4")).thenReturn(Optional.of(user));

        StreakResponse response = serviceAt("2026-05-07").evaluateStreak("user-4");

        assertEquals(0, response.getCurrent_streak());
        assertEquals(3, response.getLongest_streak());
        assertEquals(StreakStatus.BROKEN, response.getStreak_status());
        assertNull(response.getGrace_period_start());
    }

    @Test
    void firstWorkoutAfterBroken_startsFreshAtOne() {
        User user = createUser("user-5");
        user.setCurrent_streak(0);
        user.setLongest_streak(4);
        user.setStreak_status(StreakStatus.BROKEN);
        user.setLast_workout_date(LocalDate.of(2026, 5, 1));
        when(userRepository.findById("user-5")).thenReturn(Optional.of(user));

        StreakResponse response = serviceAt("2026-05-10").onWorkoutLogged("user-5");

        assertEquals(1, response.getCurrent_streak());
        assertEquals(4, response.getLongest_streak());
        assertEquals(StreakStatus.ACTIVE, response.getStreak_status());
        assertEquals(LocalDate.of(2026, 5, 10), user.getLast_workout_date());
    }

    private StreakService serviceAt(String dateIso) {
        Instant instant = LocalDate.parse(dateIso).atStartOfDay().toInstant(ZoneOffset.UTC);
        Clock fixedClock = Clock.fixed(instant, ZoneOffset.UTC);
        return new StreakService(userRepository, notificationService, fixedClock);
    }

    private User createUser(String subId) {
        User user = new User();
        user.setSub_id(subId);
        user.setStreak_status(StreakStatus.BROKEN);
        return user;
    }
}
