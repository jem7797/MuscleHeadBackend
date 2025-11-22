package com.MuscleHead.MuscleHead.Workout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("workout/api/")
public class WorkoutController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutController.class);

    @Autowired
    private WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<Workout> createWorkout(@Valid @RequestBody Workout workout) {
        logger.info("Creating new workout for user: {}",
                workout.getUser() != null ? workout.getUser().getSub_id() : "null");
        try {
            Workout createdWorkout = workoutService.createNewWorkout(workout);
            logger.info("Successfully created workout with id: {} for user: {}",
                    createdWorkout.getWorkout_id(), createdWorkout.getUser().getSub_id());
            return ResponseEntity.ok(createdWorkout);
        } catch (Exception ex) {
            logger.error("Error creating workout for user: {}",
                    workout.getUser() != null ? workout.getUser().getSub_id() : "null", ex);
            throw ex;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workout> updateWorkout(
            @PathVariable("id") long workoutId,
            @Valid @RequestBody Workout workout) {
        logger.info("Updating workout with id: {}", workoutId);
        workout.setWorkout_id(workoutId);
        return workoutService.updateWorkoutById(workoutId, workout)
                .map(updatedWorkout -> {
                    logger.info("Successfully updated workout with id: {}", workoutId);
                    return ResponseEntity.ok(updatedWorkout);
                })
                .orElseGet(() -> {
                    logger.warn("Workout not found for update: id: {}", workoutId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable("id") long workoutId) {
        logger.info("Deleting workout with id: {}", workoutId);
        if (workoutService.deleteWorkoutById(workoutId)) {
            logger.info("Successfully deleted workout with id: {}", workoutId);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Workout not found for deletion: id: {}", workoutId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workout> getWorkoutById(@PathVariable("id") long workoutId) {
        logger.debug("Getting workout by id: {}", workoutId);
        try {
            Workout workout = workoutService.getWorkoutById(workoutId);
            logger.debug("Found workout with id: {}", workoutId);
            return ResponseEntity.ok(workout);
        } catch (RuntimeException ex) {
            logger.warn("Workout not found with id: {}", workoutId);
            throw ex;
        }
    }

    @GetMapping("/user/{subId}")
    public ResponseEntity<Page<Workout>> getWorkoutsForUser(
            @PathVariable("subId") String subId,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.debug("Getting workouts for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Workout> workouts = workoutService.getWorkoutsByUserId(subId, pageable);
        logger.debug("Found {} workouts for user: {} (page {} of {})",
                workouts.getNumberOfElements(), subId, workouts.getNumber(), workouts.getTotalPages());
        return ResponseEntity.ok(workouts);
    }
}
