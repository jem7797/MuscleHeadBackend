package com.MuscleHead.MuscleHead.Workout;

import java.time.Instant;
import java.util.List;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workouts")
@Data
@NoArgsConstructor
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long workout_id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_id", referencedColumnName = "sub_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Column(updatable = false, columnDefinition = "TIMESTAMP")
    @NotNull(message = "Workout date is required")
    private Instant date;

    private String notes;

    @NotBlank(message = "Workout name cannot be blank")
    private String workout_name;

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

    @PositiveOrZero
    private double workout_highest_lift;   
}
