package com.MuscleHead.MuscleHead.LiveSession;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "live_session_exercises")
@Data
@NoArgsConstructor
public class LiveSessionExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private LiveWorkoutSession session;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private String userId;

    @Column(name = "exercise_name", nullable = false)
    @NotNull(message = "Exercise name is required")
    private String exerciseName;

    @PositiveOrZero(message = "Sets must be 0 or greater")
    private Integer sets;

    @PositiveOrZero(message = "Reps must be 0 or greater")
    private Integer reps;

    @Column(precision = 10, scale = 2)
    @PositiveOrZero(message = "Weight must be 0 or greater")
    private BigDecimal weight;

    @Column(name = "logged_at", nullable = false, updatable = false)
    private Instant loggedAt;

    @PrePersist
    protected void onCreate() {
        if (loggedAt == null) {
            loggedAt = Instant.now();
        }
    }
}
