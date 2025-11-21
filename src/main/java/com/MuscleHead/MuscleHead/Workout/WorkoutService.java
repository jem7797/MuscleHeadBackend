package com.MuscleHead.MuscleHead.Workout;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class WorkoutService {

    @Autowired
    WorkoutRepository workoutRepository;

    @Transactional
    public Workout createNewWorkout(Workout workout) {
        if (workout == null || workout.getUser() == null || workout.getUser().getSub_id() == null) {
            throw new IllegalArgumentException("Error creating new workout");
        }
        return workoutRepository.save(workout);
    }

    @Transactional
    public boolean deleteWorkout(Workout workout) {
        if (workout == null || workout.getWorkout_id() == 0) {
            return false;
        }
        if (!workoutRepository.existsById(workout.getWorkout_id())) {
            return false;
        }
        workoutRepository.delete(workout);
        return true;
    }

    @Transactional
    public boolean deleteWorkoutById(long workoutId) {
        if (workoutId == 0) {
            return false;
        }
        if (!workoutRepository.existsById(workoutId)) {
            return false;
        }
        workoutRepository.deleteById(workoutId);
        return true;
    }

    @Transactional
    public boolean updateWorkout(Workout updatedWorkout) {
        if (updatedWorkout == null || updatedWorkout.getWorkout_id() == 0) {
            return false;
        }

        return workoutRepository.findById(updatedWorkout.getWorkout_id())
                .map(existingWorkout -> {
                    existingWorkout.setDate(updatedWorkout.getDate());
                    existingWorkout.setNotes(updatedWorkout.getNotes());
                    existingWorkout.setWorkout_name(updatedWorkout.getWorkout_name());
                    existingWorkout.setArea_of_activation(updatedWorkout.getArea_of_activation());
                    existingWorkout.setReps(updatedWorkout.getReps());
                    existingWorkout.setSets(updatedWorkout.getSets());
                    existingWorkout.setDuration(updatedWorkout.getDuration());
                    existingWorkout.setTotal_weight_lifted(updatedWorkout.getTotal_weight_lifted());
                    existingWorkout.setUser(updatedWorkout.getUser());

                    workoutRepository.save(existingWorkout);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public java.util.Optional<Workout> updateWorkoutById(long workoutId, Workout updatedWorkout) {
        if (updatedWorkout == null) {
            return java.util.Optional.empty();
        }

        return workoutRepository.findById(workoutId)
                .map(existingWorkout -> {
                    existingWorkout.setDate(updatedWorkout.getDate());
                    existingWorkout.setNotes(updatedWorkout.getNotes());
                    existingWorkout.setWorkout_name(updatedWorkout.getWorkout_name());
                    existingWorkout.setArea_of_activation(updatedWorkout.getArea_of_activation());
                    existingWorkout.setReps(updatedWorkout.getReps());
                    existingWorkout.setSets(updatedWorkout.getSets());
                    existingWorkout.setDuration(updatedWorkout.getDuration());
                    existingWorkout.setTotal_weight_lifted(updatedWorkout.getTotal_weight_lifted());
                    existingWorkout.setUser(updatedWorkout.getUser());

                    return workoutRepository.save(existingWorkout);
                });
    }

    public Workout getWorkoutById(long workoutId) {
        return workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found: " + workoutId));
    }

    public List<Workout> getWorkoutsByUserId(String subId) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return workoutRepository.findByUser_SubId(subId);
    }
}
