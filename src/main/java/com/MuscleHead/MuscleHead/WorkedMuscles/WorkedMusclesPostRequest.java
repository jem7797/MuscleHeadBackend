package com.MuscleHead.MuscleHead.WorkedMuscles;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkedMusclesPostRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotNull(message = "exercises is required")
    @Valid
    private List<ExerciseInput> exercises;

    @Data
    public static class ExerciseInput {
        @NotNull(message = "exerciseId is required")
        private Long exerciseId;
        private Integer sets;
        private Integer reps;
        private Double weight;
    }
}
