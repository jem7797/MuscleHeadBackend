package com.MuscleHead.MuscleHead.Workout.WorkoutSchedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.config.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("workoutSchedule/api/")
public class WorkoutScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutScheduleController.class);

    @Autowired
    private WorkoutScheduleService workoutScheduleService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/{subId}")
    public ResponseEntity<java.util.List<WorkoutSchedule>> getWorkoutSchedules(@PathVariable("subId") String subId) {
        String authSubId = SecurityUtils.getCurrentUserSub();
        if (authSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authSubId.equals(subId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(workoutScheduleService.findByUserSubId(subId));
    }

    @PostMapping
    public ResponseEntity<WorkoutSchedule> createWorkoutSchedule(@Valid @RequestBody WorkoutScheduleRequest request) {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            logger.error("Attempted to create workout schedule without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(subId)
                .orElseThrow(() -> new RuntimeException("User not found: " + subId));

        WorkoutSchedule created = workoutScheduleService.create(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkoutSchedule> patchWorkoutSchedule(
            @PathVariable("id") Long id,
            @Valid @RequestBody WorkoutSchedulePatchRequest request) {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            logger.error("Attempted to patch workout schedule without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return workoutScheduleService.patch(id, subId, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
