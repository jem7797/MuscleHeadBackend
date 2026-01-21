package com.MuscleHead.MuscleHead.WorkoutTemplate;

import java.util.List;

import com.MuscleHead.MuscleHead.ExerciseInstance.ExerciseInstanceRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorkoutTemplateRequest {
    @NotBlank(message = "Routine name cannot be blank")
    private String name;

    @NotNull(message = "Exercises list is required")
    @NotEmpty(message = "Exercises list cannot be empty")
    @Valid
    private List<ExerciseInstanceRequest> exercises;
}
