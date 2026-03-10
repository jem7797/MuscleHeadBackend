package com.MuscleHead.MuscleHead.LiveSession;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveSessionExerciseDto {

    private UUID id;
    private String userId;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private BigDecimal weight;
    private Instant loggedAt;

    public static LiveSessionExerciseDto from(LiveSessionExercise entity) {
        return new LiveSessionExerciseDto(
                entity.getId(),
                entity.getUserId(),
                entity.getExerciseName(),
                entity.getSets(),
                entity.getReps(),
                entity.getWeight(),
                entity.getLoggedAt());
    }
}
