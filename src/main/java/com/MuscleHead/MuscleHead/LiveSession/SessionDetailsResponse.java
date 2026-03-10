package com.MuscleHead.MuscleHead.LiveSession;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDetailsResponse {

    private UUID id;
    private String hostUserId;
    private String guestUserId;
    private String status;
    private Instant createdAt;
    private List<LiveSessionExerciseDto> hostExercises;
    private List<LiveSessionExerciseDto> guestExercises;
}
