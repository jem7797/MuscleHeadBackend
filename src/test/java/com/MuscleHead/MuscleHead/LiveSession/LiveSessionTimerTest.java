package com.MuscleHead.MuscleHead.LiveSession;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class LiveSessionTimerTest {

    @Test
    void runningTimer_includesCurrentSegment() {
        LiveWorkoutSession session = new LiveWorkoutSession();
        session.setId(UUID.randomUUID());
        session.setTimerState(TimerState.RUNNING);
        session.setTimerElapsedSeconds(60);
        session.setTimerStartedAt(Instant.parse("2026-05-10T12:00:00Z"));

        long elapsed = LiveSessionService.computeElapsedSeconds(
                session,
                Instant.parse("2026-05-10T12:01:30Z"));

        assertEquals(150, elapsed);
    }

    @Test
    void pausedTimer_usesAccumulatedElapsedOnly() {
        LiveWorkoutSession session = new LiveWorkoutSession();
        session.setTimerState(TimerState.PAUSED);
        session.setTimerElapsedSeconds(300);
        session.setTimerStartedAt(null);

        long elapsed = LiveSessionService.computeElapsedSeconds(
                session,
                Instant.parse("2026-05-10T12:05:00Z"));

        assertEquals(300, elapsed);
    }

    @Test
    void stoppedTimer_returnsAccumulatedElapsed() {
        LiveWorkoutSession session = new LiveWorkoutSession();
        session.setTimerState(TimerState.STOPPED);
        session.setTimerElapsedSeconds(900);

        long elapsed = LiveSessionService.computeElapsedSeconds(
                session,
                Instant.parse("2026-05-10T13:00:00Z"));

        assertEquals(900, elapsed);
    }
}
