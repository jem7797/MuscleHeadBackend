package com.MuscleHead.MuscleHead.Workout.WorkoutSchedule;

import com.MuscleHead.MuscleHead.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workout_schedule")
@Data
@NoArgsConstructor
public class WorkoutSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_sub_id", referencedColumnName = "sub_id", nullable = false)
    @NotNull(message = "User is required")
    @JsonIgnore
    private User user;

    /**
     * Day of week: 1 = Monday, 7 = Sunday (ISO 8601).
     */
    @NotNull(message = "Day of week is required")
    @Min(value = 1, message = "Day must be 1-7 (Monday-Sunday)")
    @Max(value = 7, message = "Day must be 1-7 (Monday-Sunday)")
    private Integer day_of_the_week;


    private String label = "";
}
