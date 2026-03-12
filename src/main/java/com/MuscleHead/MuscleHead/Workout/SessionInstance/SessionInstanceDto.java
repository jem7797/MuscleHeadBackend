package com.MuscleHead.MuscleHead.Workout.SessionInstance;

import java.util.List;

import com.MuscleHead.MuscleHead.Movement.Movement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionInstanceDto {

    private long workout_exercise_id;
    private Long exerciseId;
    private String exerciseName;
    private List<String> area_of_activation;
    private int reps;
    private int sets;
    private double duration;
    private double total_weight_lifted;
    private double workout_highest_lift;

    public static SessionInstanceDto from(SessionInstance si) {
        if (si == null) return null;
        Movement m = si.getMovement();
        return new SessionInstanceDto(
                si.getWorkout_exercise_id(),
                m != null ? m.getId() : null,
                m != null ? m.getName() : null,
                si.getArea_of_activation(),
                si.getReps(),
                si.getSets(),
                si.getDuration(),
                si.getTotal_weight_lifted(),
                si.getWorkout_highest_lift()
        );
    }
}
