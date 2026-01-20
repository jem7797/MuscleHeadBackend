package com.MuscleHead.MuscleHead.Workout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.config.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("workout-session/api/")
public class WorkoutSessionController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutSessionController.class);

    @Autowired
    private WorkoutSessionService workoutSessionService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<WorkoutSessionResponse> createWorkoutSession(@Valid @RequestBody WorkoutSessionRequest request) {
        logger.info("Creating new workout session");
        
        // Resolve authenticated user
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            logger.error("Attempted to create workout session without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(subId)
                .orElseThrow(() -> {
                    logger.error("User not found with sub_id: {}", subId);
                    return new RuntimeException("User not found: " + subId);
                });

        try {
            WorkoutSession createdSession = workoutSessionService.createWorkoutSession(user, request);
            logger.info("Successfully created workout session with id: {} for user: {}",
                    createdSession.getId(), subId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new WorkoutSessionResponse(createdSession.getId()));
        } catch (Exception ex) {
            logger.error("Error creating workout session for user: {}", subId, ex);
            throw ex;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutSession> updateWorkoutSession(
            @PathVariable("id") long workoutSessionId,
            @Valid @RequestBody WorkoutSession workoutSession) {
        logger.info("Updating workout session with id: {}", workoutSessionId);
        workoutSession.setId(workoutSessionId);
        return workoutSessionService.updateWorkoutSessionById(workoutSessionId, workoutSession)
                .map(updatedWorkoutSession -> {
                    logger.info("Successfully updated workout session with id: {}", workoutSessionId);
                    return ResponseEntity.ok(updatedWorkoutSession);
                })
                .orElseGet(() -> {
                    logger.warn("Workout session not found for update: id: {}", workoutSessionId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkoutSession(@PathVariable("id") long workoutSessionId) {
        logger.info("Deleting workout session with id: {}", workoutSessionId);
        if (workoutSessionService.deleteWorkoutSessionById(workoutSessionId)) {
            logger.info("Successfully deleted workout session with id: {}", workoutSessionId);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Workout session not found for deletion: id: {}", workoutSessionId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSession> getWorkoutSessionById(@PathVariable("id") long workoutSessionId) {
        logger.debug("Getting workout session by id: {}", workoutSessionId);
        try {
            WorkoutSession workoutSession = workoutSessionService.getWorkoutSessionById(workoutSessionId);
            logger.debug("Found workout session with id: {}", workoutSessionId);
            return ResponseEntity.ok(workoutSession);
        } catch (RuntimeException ex) {
            logger.warn("Workout session not found with id: {}", workoutSessionId);
            throw ex;
        }
    }

    @GetMapping("/user/{subId}")
    public ResponseEntity<Page<WorkoutSession>> getWorkoutSessionsForUser(
            @PathVariable("subId") String subId,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.debug("Getting workout sessions for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        Page<WorkoutSession> workoutSessions = workoutSessionService.getWorkoutSessionsByUserId(subId, pageable);
        logger.debug("Found {} workout sessions for user: {} (page {} of {})",
                workoutSessions.getNumberOfElements(), subId, workoutSessions.getNumber(), workoutSessions.getTotalPages());
        return ResponseEntity.ok(workoutSessions);
    }
}
