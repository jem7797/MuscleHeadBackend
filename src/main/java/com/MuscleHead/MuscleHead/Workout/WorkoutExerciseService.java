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
public class WorkoutExerciseService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutExerciseService.class);

    @Autowired
    WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    UserRepository userRepository;

    @Transactional
    public WorkoutExercise createNewWorkoutExercise(WorkoutExercise workoutExercise) {
        logger.debug("Creating new workout exercise for user: {}",
                workoutExercise != null && workoutExercise.getUser() != null ? workoutExercise.getUser().getSub_id() : "null");
        if (workoutExercise == null || workoutExercise.getUser() == null || workoutExercise.getUser().getSub_id() == null) {
            logger.error("Attempted to create workout exercise with null workout exercise, user, or sub_id");
            throw new IllegalArgumentException("Error creating new workout exercise");
        }
        WorkoutExercise savedWorkoutExercise = workoutExerciseRepository.save(workoutExercise);
        logger.info("Workout exercise created successfully with id: {} for user: {}",
                savedWorkoutExercise.getWorkout_exercise_id(), savedWorkoutExercise.getUser().getSub_id());

        // Update user's highest weight lifted if workout exercise's highest lift is greater
        updateUserHighestWeightLifted(savedWorkoutExercise);

        return savedWorkoutExercise;
    }

    @Transactional
    public boolean deleteWorkoutExercise(WorkoutExercise workoutExercise) {
        if (workoutExercise == null || workoutExercise.getWorkout_exercise_id() == 0) {
            return false;
        }
        if (!workoutExerciseRepository.existsById(workoutExercise.getWorkout_exercise_id())) {
            return false;
        }
        workoutExerciseRepository.delete(workoutExercise);
        return true;
    }

    @Transactional
    public boolean deleteWorkoutExerciseById(long workoutExerciseId) {
        logger.debug("Deleting workout exercise with id: {}", workoutExerciseId);
        if (workoutExerciseId == 0) {
            logger.warn("Attempted to delete workout exercise with invalid id: 0");
            return false;
        }
        if (!workoutExerciseRepository.existsById(workoutExerciseId)) {
            logger.warn("Workout exercise not found for deletion: id: {}", workoutExerciseId);
            return false;
        }
        workoutExerciseRepository.deleteById(workoutExerciseId);
        logger.info("Workout exercise deleted successfully with id: {}", workoutExerciseId);
        return true;
    }

    @Transactional
    public boolean updateWorkoutExercise(WorkoutExercise updatedWorkoutExercise) {
        if (updatedWorkoutExercise == null || updatedWorkoutExercise.getWorkout_exercise_id() == 0) {
            return false;
        }

        return workoutExerciseRepository.findById(updatedWorkoutExercise.getWorkout_exercise_id())
                .map(existingWorkoutExercise -> {
                    existingWorkoutExercise.setNotes(updatedWorkoutExercise.getNotes());
                    existingWorkoutExercise.setExercise(updatedWorkoutExercise.getExercise());
                    existingWorkoutExercise.setArea_of_activation(updatedWorkoutExercise.getArea_of_activation());
                    existingWorkoutExercise.setReps(updatedWorkoutExercise.getReps());
                    existingWorkoutExercise.setSets(updatedWorkoutExercise.getSets());
                    existingWorkoutExercise.setDuration(updatedWorkoutExercise.getDuration());
                    existingWorkoutExercise.setTotal_weight_lifted(updatedWorkoutExercise.getTotal_weight_lifted());
                    existingWorkoutExercise.setWorkout_highest_lift(updatedWorkoutExercise.getWorkout_highest_lift());
                    existingWorkoutExercise.setUser(updatedWorkoutExercise.getUser());
                    existingWorkoutExercise.setWorkoutSession(updatedWorkoutExercise.getWorkoutSession());

                    WorkoutExercise savedWorkoutExercise = workoutExerciseRepository.save(existingWorkoutExercise);

                    // Update user's highest weight lifted if workout exercise's highest lift is greater
                    updateUserHighestWeightLifted(savedWorkoutExercise);

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public java.util.Optional<WorkoutExercise> updateWorkoutExerciseById(long workoutExerciseId, WorkoutExercise updatedWorkoutExercise) {
        logger.debug("Updating workout exercise with id: {}", workoutExerciseId);
        if (updatedWorkoutExercise == null) {
            logger.error("Attempted to update workout exercise with null workout exercise object");
            return java.util.Optional.empty();
        }

        return workoutExerciseRepository.findById(workoutExerciseId)
                .map(existingWorkoutExercise -> {
                    logger.debug("Found existing workout exercise, updating fields for id: {}", workoutExerciseId);
                    existingWorkoutExercise.setNotes(updatedWorkoutExercise.getNotes());
                    existingWorkoutExercise.setExercise(updatedWorkoutExercise.getExercise());
                    existingWorkoutExercise.setArea_of_activation(updatedWorkoutExercise.getArea_of_activation());
                    existingWorkoutExercise.setReps(updatedWorkoutExercise.getReps());
                    existingWorkoutExercise.setSets(updatedWorkoutExercise.getSets());
                    existingWorkoutExercise.setDuration(updatedWorkoutExercise.getDuration());
                    existingWorkoutExercise.setTotal_weight_lifted(updatedWorkoutExercise.getTotal_weight_lifted());
                    existingWorkoutExercise.setWorkout_highest_lift(updatedWorkoutExercise.getWorkout_highest_lift());
                    existingWorkoutExercise.setUser(updatedWorkoutExercise.getUser());
                    existingWorkoutExercise.setWorkoutSession(updatedWorkoutExercise.getWorkoutSession());

                    WorkoutExercise savedWorkoutExercise = workoutExerciseRepository.save(existingWorkoutExercise);
                    logger.info("Workout exercise updated successfully with id: {}", workoutExerciseId);

                    // Update user's highest weight lifted if workout exercise's highest lift is greater
                    updateUserHighestWeightLifted(savedWorkoutExercise);

                    return savedWorkoutExercise;
                });
    }

    public WorkoutExercise getWorkoutExerciseById(long workoutExerciseId) {
        logger.debug("Getting workout exercise by id: {}", workoutExerciseId);
        return workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> {
                    logger.warn("Workout exercise not found with id: {}", workoutExerciseId);
                    return new RuntimeException("Workout exercise not found: " + workoutExerciseId);
                });
    }

    public List<WorkoutExercise> getWorkoutExercisesByUserId(String subId) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return workoutExerciseRepository.findByUser_SubId(subId);
    }

    public Page<WorkoutExercise> getWorkoutExercisesByUserId(String subId, Pageable pageable) {
        logger.debug("Getting workout exercises for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        if (subId == null || subId.isBlank()) {
            logger.error("Attempted to get workout exercises with null or blank sub_id");
            throw new IllegalArgumentException("User id must not be blank");
        }
        Page<WorkoutExercise> workoutExercises = workoutExerciseRepository.findByUser_SubId(subId, pageable);
        logger.debug("Found {} workout exercises for user: {} (total: {})",
                workoutExercises.getNumberOfElements(), subId, workoutExercises.getTotalElements());
        return workoutExercises;
    }

    public List<WorkoutExercise> getWorkoutExercisesBySessionId(long sessionId) {
        return workoutExerciseRepository.findByWorkoutSessionId(sessionId);
    }

    /**
     * Compares the workout exercise's highest lift with the user's highest weight lifted.
     * If the workout exercise's highest lift is greater, updates the user's highest weight
     * lifted.
     * 
     * @param workoutExercise The workout exercise to compare
     */
    @Transactional
    private void updateUserHighestWeightLifted(WorkoutExercise workoutExercise) {
        if (workoutExercise == null || workoutExercise.getUser() == null || workoutExercise.getUser().getSub_id() == null) {
            logger.debug("Cannot update user highest weight lifted: workout exercise or user is null");
            return;
        }

        double workoutHighestLift = workoutExercise.getWorkout_highest_lift();
        if (workoutHighestLift <= 0) {
            logger.debug("Workout exercise highest lift is 0 or negative, skipping update");
            return;
        }

        Optional<User> userOpt = userRepository.findById(workoutExercise.getUser().getSub_id());
        if (userOpt.isEmpty()) {
            logger.warn("User not found with sub_id: {}", workoutExercise.getUser().getSub_id());
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
                    "Workout exercise highest lift ({}) is not greater than user's highest weight lifted ({}), no update needed",
                    workoutHighestLift, userHighestWeightLifted);
        }
    }
}
