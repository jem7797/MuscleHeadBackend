package com.MuscleHead.MuscleHead.WorkedMuscles;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MuscleWithExpiry {

    private String muscleId;
    private Instant expiresAt;
}
