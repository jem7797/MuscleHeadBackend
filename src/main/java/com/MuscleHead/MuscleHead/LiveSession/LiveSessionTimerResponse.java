package com.MuscleHead.MuscleHead.LiveSession;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveSessionTimerResponse {

    /** Elapsed workout time in seconds, computed on the server. */
    private long elapsedSeconds;

    private TimerState timerState;

    /** Current server time (UTC) so clients can correct for clock drift. */
    private Instant serverTime;
}
