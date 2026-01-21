package com.MuscleHead.MuscleHead.Workout;

import java.util.List;

import com.MuscleHead.MuscleHead.Movement.Movement;
import com.MuscleHead.MuscleHead.User.User;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
@Table(name = "workout_exercises")
@Data
@NoArgsConstructor
public class WorkoutExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long workout_exercise_id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_id", referencedColumnName = "sub_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_session_id", nullable = false)
    @NotNull(message = "Workout session is required")
    private WorkoutSession workoutSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    @NotNull(message = "Exercise is required")
    private Movement exercise;

    @ElementCollection
    private List<String> area_of_activation;

    @Positive(message = "Reps must be a positive number")
    private int reps;

    @Positive(message = "Sets must be a positive number")
    private int sets;

    @PositiveOrZero(message = "Duration cannot be negative")
    private double duration;

    @PositiveOrZero(message = "Total weight lifted cannot be negative")
    private double total_weight_lifted;

    @PositiveOrZero(message = "Workout highest lift cannot be negative")
    private double workout_highest_lift;

    private String notes;
}
