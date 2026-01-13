package com.MuscleHead.MuscleHead.Routine;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routine_exercises")
@Data
@NoArgsConstructor
public class RoutineExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "routine_id", nullable = false)
    @JsonIgnore
    @NotNull(message = "Routine is required")
    private Routine routine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    @NotNull(message = "Exercise is required")
    private Exercise exercise;

    @PositiveOrZero(message = "Order index cannot be negative")
    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    @Positive(message = "Target reps must be a positive number")
    private Integer targetReps;

    @Positive(message = "Target sets must be a positive number")
    private Integer targetSets;
}
