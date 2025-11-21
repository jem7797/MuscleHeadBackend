package com.MuscleHead.MuscleHead.Workout;

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

    @Autowired
    private WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<Workout> createWorkout(@Valid @RequestBody Workout workout) {
        Workout createdWorkout = workoutService.createNewWorkout(workout);
        return ResponseEntity.ok(createdWorkout);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workout> updateWorkout(
            @PathVariable("id") long workoutId,
            @Valid @RequestBody Workout workout) {
        workout.setWorkout_id(workoutId);
        return workoutService.updateWorkoutById(workoutId, workout)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable("id") long workoutId) {
        if (workoutService.deleteWorkoutById(workoutId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workout> getWorkoutById(@PathVariable("id") long workoutId) {
        Workout workout = workoutService.getWorkoutById(workoutId);
        return ResponseEntity.ok(workout);
    }

    @GetMapping("/user/{subId}")
    public ResponseEntity<Page<Workout>> getWorkoutsForUser(
            @PathVariable("subId") String subId,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Workout> workouts = workoutService.getWorkoutsByUserId(subId, pageable);
        return ResponseEntity.ok(workouts);
    }
}
