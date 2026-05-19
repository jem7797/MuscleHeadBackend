package com.MuscleHead.MuscleHead.Workout.SessionInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class SessionInstanceLastAttemptTest {

    @Test
    void expandSets_producesOneEntryPerSet() {
        SessionInstance instance = new SessionInstance();
        instance.setSets(3);
        instance.setReps(8);
        instance.setWorkout_highest_lift(135);

        List<LastExerciseSetResponse> sets = SessionInstanceService.expandSets(instance);

        assertEquals(3, sets.size());
        assertEquals(8, sets.get(0).getReps());
        assertEquals(135, sets.get(1).getWeight());
        assertEquals(8, sets.get(2).getReps());
    }

    @Test
    void expandSets_emptyWhenSetCountZero() {
        SessionInstance instance = new SessionInstance();
        instance.setSets(0);
        instance.setReps(10);
        instance.setWorkout_highest_lift(100);

        assertTrue(SessionInstanceService.expandSets(instance).isEmpty());
    }
}
