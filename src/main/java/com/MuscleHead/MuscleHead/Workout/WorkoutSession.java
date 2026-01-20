package com.MuscleHead.MuscleHead.Workout;

import java.time.Instant;
import java.util.List;

import com.MuscleHead.MuscleHead.Routine.Routine;
import com.MuscleHead.MuscleHead.User.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workout_sessions")
@Data
@NoArgsConstructor
public class WorkoutSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_id", referencedColumnName = "sub_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Column(updatable = false, columnDefinition = "TIMESTAMP")
    @NotNull(message = "Workout session date is required")
    private Instant date;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private Routine routine;

    @PositiveOrZero(message = "Total weight lifted cannot be negative")
    private Double total_weight_lifted;

    @PositiveOrZero(message = "Session highest lift cannot be negative")
    private Double session_highest_lift;

    @PositiveOrZero(message = "Total duration cannot be negative")
    private Double total_duration;

    @OneToMany(mappedBy = "workoutSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutExercise> workoutExercises;
}
