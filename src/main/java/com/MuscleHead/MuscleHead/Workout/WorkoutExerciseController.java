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
@RequestMapping("workout-exercise/api/")
public class WorkoutExerciseController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutExerciseController.class);

    @Autowired
    private WorkoutExerciseService workoutExerciseService;

    @PostMapping
    public ResponseEntity<WorkoutExercise> createWorkoutExercise(@Valid @RequestBody WorkoutExercise workoutExercise) {
        logger.info("Creating new workout exercise for user: {}",
                workoutExercise.getUser() != null ? workoutExercise.getUser().getSub_id() : "null");
        try {
            WorkoutExercise createdWorkoutExercise = workoutExerciseService.createNewWorkoutExercise(workoutExercise);
            logger.info("Successfully created workout exercise with id: {} for user: {}",
                    createdWorkoutExercise.getWorkout_exercise_id(), createdWorkoutExercise.getUser().getSub_id());
            return ResponseEntity.ok(createdWorkoutExercise);
        } catch (Exception ex) {
            logger.error("Error creating workout exercise for user: {}",
                    workoutExercise.getUser() != null ? workoutExercise.getUser().getSub_id() : "null", ex);
            throw ex;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutExercise> updateWorkoutExercise(
            @PathVariable("id") long workoutExerciseId,
            @Valid @RequestBody WorkoutExercise workoutExercise) {
        logger.info("Updating workout exercise with id: {}", workoutExerciseId);
        workoutExercise.setWorkout_exercise_id(workoutExerciseId);
        return workoutExerciseService.updateWorkoutExerciseById(workoutExerciseId, workoutExercise)
                .map(updatedWorkoutExercise -> {
                    logger.info("Successfully updated workout exercise with id: {}", workoutExerciseId);
                    return ResponseEntity.ok(updatedWorkoutExercise);
                })
                .orElseGet(() -> {
                    logger.warn("Workout exercise not found for update: id: {}", workoutExerciseId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkoutExercise(@PathVariable("id") long workoutExerciseId) {
        logger.info("Deleting workout exercise with id: {}", workoutExerciseId);
        if (workoutExerciseService.deleteWorkoutExerciseById(workoutExerciseId)) {
            logger.info("Successfully deleted workout exercise with id: {}", workoutExerciseId);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Workout exercise not found for deletion: id: {}", workoutExerciseId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutExercise> getWorkoutExerciseById(@PathVariable("id") long workoutExerciseId) {
        logger.debug("Getting workout exercise by id: {}", workoutExerciseId);
        try {
            WorkoutExercise workoutExercise = workoutExerciseService.getWorkoutExerciseById(workoutExerciseId);
            logger.debug("Found workout exercise with id: {}", workoutExerciseId);
            return ResponseEntity.ok(workoutExercise);
        } catch (RuntimeException ex) {
            logger.warn("Workout exercise not found with id: {}", workoutExerciseId);
            throw ex;
        }
    }

    @GetMapping("/user/{subId}")
    public ResponseEntity<Page<WorkoutExercise>> getWorkoutExercisesForUser(
            @PathVariable("subId") String subId,
            @PageableDefault(size = 10, sort = "workout_exercise_id", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.debug("Getting workout exercises for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        Page<WorkoutExercise> workoutExercises = workoutExerciseService.getWorkoutExercisesByUserId(subId, pageable);
        logger.debug("Found {} workout exercises for user: {} (page {} of {})",
                workoutExercises.getNumberOfElements(), subId, workoutExercises.getNumber(), workoutExercises.getTotalPages());
        return ResponseEntity.ok(workoutExercises);
    }
}
