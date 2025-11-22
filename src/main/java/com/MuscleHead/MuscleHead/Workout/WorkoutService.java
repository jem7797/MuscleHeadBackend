package com.MuscleHead.MuscleHead.Workout;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class WorkoutService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutService.class);

    @Autowired
    WorkoutRepository workoutRepository;

    @Transactional
    public Workout createNewWorkout(Workout workout) {
        logger.debug("Creating new workout for user: {}",
                workout != null && workout.getUser() != null ? workout.getUser().getSub_id() : "null");
        if (workout == null || workout.getUser() == null || workout.getUser().getSub_id() == null) {
            logger.error("Attempted to create workout with null workout, user, or sub_id");
            throw new IllegalArgumentException("Error creating new workout");
        }
        Workout savedWorkout = workoutRepository.save(workout);
        logger.info("Workout created successfully with id: {} for user: {}",
                savedWorkout.getWorkout_id(), savedWorkout.getUser().getSub_id());
        return savedWorkout;
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
        logger.debug("Deleting workout with id: {}", workoutId);
        if (workoutId == 0) {
            logger.warn("Attempted to delete workout with invalid id: 0");
            return false;
        }
        if (!workoutRepository.existsById(workoutId)) {
            logger.warn("Workout not found for deletion: id: {}", workoutId);
            return false;
        }
        workoutRepository.deleteById(workoutId);
        logger.info("Workout deleted successfully with id: {}", workoutId);
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
        logger.debug("Updating workout with id: {}", workoutId);
        if (updatedWorkout == null) {
            logger.error("Attempted to update workout with null workout object");
            return java.util.Optional.empty();
        }

        return workoutRepository.findById(workoutId)
                .map(existingWorkout -> {
                    logger.debug("Found existing workout, updating fields for id: {}", workoutId);
                    existingWorkout.setDate(updatedWorkout.getDate());
                    existingWorkout.setNotes(updatedWorkout.getNotes());
                    existingWorkout.setWorkout_name(updatedWorkout.getWorkout_name());
                    existingWorkout.setArea_of_activation(updatedWorkout.getArea_of_activation());
                    existingWorkout.setReps(updatedWorkout.getReps());
                    existingWorkout.setSets(updatedWorkout.getSets());
                    existingWorkout.setDuration(updatedWorkout.getDuration());
                    existingWorkout.setTotal_weight_lifted(updatedWorkout.getTotal_weight_lifted());
                    existingWorkout.setUser(updatedWorkout.getUser());

                    Workout savedWorkout = workoutRepository.save(existingWorkout);
                    logger.info("Workout updated successfully with id: {}", workoutId);
                    return savedWorkout;
                });
    }

    public Workout getWorkoutById(long workoutId) {
        logger.debug("Getting workout by id: {}", workoutId);
        return workoutRepository.findById(workoutId)
                .orElseThrow(() -> {
                    logger.warn("Workout not found with id: {}", workoutId);
                    return new RuntimeException("Workout not found: " + workoutId);
                });
    }

    public List<Workout> getWorkoutsByUserId(String subId) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return workoutRepository.findByUser_SubId(subId);
    }

    public Page<Workout> getWorkoutsByUserId(String subId, Pageable pageable) {
        logger.debug("Getting workouts for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        if (subId == null || subId.isBlank()) {
            logger.error("Attempted to get workouts with null or blank sub_id");
            throw new IllegalArgumentException("User id must not be blank");
        }
        Page<Workout> workouts = workoutRepository.findByUser_SubId(subId, pageable);
        logger.debug("Found {} workouts for user: {} (total: {})",
                workouts.getNumberOfElements(), subId, workouts.getTotalElements());
        return workouts;
    }
}
