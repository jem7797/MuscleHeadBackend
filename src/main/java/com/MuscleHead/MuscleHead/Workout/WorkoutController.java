package com.MuscleHead.MuscleHead.Workout;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    @PutMapping
    public ResponseEntity<Boolean> updateWorkout(@Valid @RequestBody Workout workout) {
        boolean updated = workoutService.updateWorkout(workout);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(true);
    }

    @DeleteMapping
    public ResponseEntity<Boolean> deleteWorkout(@Valid @RequestBody Workout workout) {
        boolean deleted = workoutService.deleteWorkout(workout);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workout> getWorkoutById(@PathVariable("id") long workoutId) {
        Workout workout = workoutService.getWorkoutById(workoutId);
        return ResponseEntity.ok(workout);
    }

    @GetMapping("/user/{subId}")
    public ResponseEntity<List<Workout>> getWorkoutsForUser(@PathVariable("subId") String subId) {
        List<Workout> workouts = workoutService.getWorkoutsByUserId(subId);
        return ResponseEntity.ok(workouts);
    }
}
