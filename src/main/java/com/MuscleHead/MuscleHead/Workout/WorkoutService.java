package com.MuscleHead.MuscleHead.Workout;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class WorkoutService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutService.class);

    @Autowired
    WorkoutRepository workoutRepository;

    @Autowired
    UserRepository userRepository;

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

        // Update user's highest weight lifted if workout's highest lift is greater
        updateUserHighestWeightLifted(savedWorkout);

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
                    existingWorkout.setWorkout_highest_lift(updatedWorkout.getWorkout_highest_lift());
                    existingWorkout.setUser(updatedWorkout.getUser());

                    Workout savedWorkout = workoutRepository.save(existingWorkout);

                    // Update user's highest weight lifted if workout's highest lift is greater
                    updateUserHighestWeightLifted(savedWorkout);

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
                    existingWorkout.setWorkout_highest_lift(updatedWorkout.getWorkout_highest_lift());
                    existingWorkout.setUser(updatedWorkout.getUser());

                    Workout savedWorkout = workoutRepository.save(existingWorkout);
                    logger.info("Workout updated successfully with id: {}", workoutId);

                    // Update user's highest weight lifted if workout's highest lift is greater
                    updateUserHighestWeightLifted(savedWorkout);

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

    /**
     * Compares the workout's highest lift with the user's highest weight lifted.
     * If the workout's highest lift is greater, updates the user's highest weight
     * lifted.
     * 
     * @param workout The workout to compare
     */
    @Transactional
    private void updateUserHighestWeightLifted(Workout workout) {
        if (workout == null || workout.getUser() == null || workout.getUser().getSub_id() == null) {
            logger.debug("Cannot update user highest weight lifted: workout or user is null");
            return;
        }

        double workoutHighestLift = workout.getWorkout_highest_lift();
        if (workoutHighestLift <= 0) {
            logger.debug("Workout highest lift is 0 or negative, skipping update");
            return;
        }

        Optional<User> userOpt = userRepository.findById(workout.getUser().getSub_id());
        if (userOpt.isEmpty()) {
            logger.warn("User not found with sub_id: {}", workout.getUser().getSub_id());
            return;
        }

        User user = userOpt.get();
        double userHighestWeightLifted = user.getHighest_weight_lifted();

        if (workoutHighestLift > userHighestWeightLifted) {
            logger.info("Updating user's highest weight lifted from {} to {} for user: {}",
                    userHighestWeightLifted, workoutHighestLift, user.getSub_id());
            user.setHighest_weight_lifted(workoutHighestLift);
            userRepository.save(user);
        } else {
            logger.debug(
                    "Workout highest lift ({}) is not greater than user's highest weight lifted ({}), no update needed",
                    workoutHighestLift, userHighestWeightLifted);
        }
    }
}
