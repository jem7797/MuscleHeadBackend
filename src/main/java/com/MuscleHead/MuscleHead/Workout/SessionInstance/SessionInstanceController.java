package com.MuscleHead.MuscleHead.Workout.SessionInstance;

import java.util.List;

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
@RequestMapping("sessionInstance/api/")
public class SessionInstanceController {

    private static final Logger logger = LoggerFactory.getLogger(SessionInstanceController.class);

    @Autowired
    private SessionInstanceService sessionInstanceService;

    @PostMapping
    public ResponseEntity<SessionInstanceDto> createSessionInstance(@Valid @RequestBody SessionInstance sessionInstance) {
        logger.info("Creating new session instance for user: {}",
                sessionInstance.getUser() != null ? sessionInstance.getUser().getSub_id() : "null");
        try {
            SessionInstance createdSessionInstance = sessionInstanceService.createNewSessionInstance(sessionInstance);
            SessionInstance withMovement = sessionInstanceService.getSessionInstanceById(createdSessionInstance.getWorkout_exercise_id());
            logger.info("Successfully created session instance with id: {} for user: {}",
                    createdSessionInstance.getWorkout_exercise_id(), createdSessionInstance.getUser().getSub_id());
            return ResponseEntity.ok(SessionInstanceDto.from(withMovement));
        } catch (Exception ex) {
            logger.error("Error creating session instance for user: {}",
                    sessionInstance.getUser() != null ? sessionInstance.getUser().getSub_id() : "null", ex);
            throw ex;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionInstanceDto> updateSessionInstance(
            @PathVariable("id") long sessionInstanceId,
            @Valid @RequestBody SessionInstance sessionInstance) {
        logger.info("Updating session instance with id: {}", sessionInstanceId);
        sessionInstance.setWorkout_exercise_id(sessionInstanceId);
        return sessionInstanceService.updateSessionInstanceById(sessionInstanceId, sessionInstance)
                .map(updatedSessionInstance -> {
                    SessionInstance withMovement = sessionInstanceService.getSessionInstanceById(sessionInstanceId);
                    logger.info("Successfully updated session instance with id: {}", sessionInstanceId);
                    return ResponseEntity.ok(SessionInstanceDto.from(withMovement));
                })
                .orElseGet(() -> {
                    logger.warn("Session instance not found for update: id: {}", sessionInstanceId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSessionInstance(@PathVariable("id") long sessionInstanceId) {
        logger.info("Deleting session instance with id: {}", sessionInstanceId);
        if (sessionInstanceService.deleteSessionInstanceById(sessionInstanceId)) {
            logger.info("Successfully deleted session instance with id: {}", sessionInstanceId);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Session instance not found for deletion: id: {}", sessionInstanceId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<SessionInstanceDto>> getSessionInstancesBySession(
            @PathVariable("sessionId") long sessionId) {
        logger.debug("Getting session instances for workout session: {}", sessionId);
        List<SessionInstance> sessionInstances = sessionInstanceService.getSessionInstancesBySessionId(sessionId);
        logger.debug("Found {} exercises for workout session: {}", sessionInstances.size(), sessionId);
        return ResponseEntity.ok(sessionInstances.stream().map(SessionInstanceDto::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionInstanceDto> getSessionInstanceById(@PathVariable("id") long sessionInstanceId) {
        logger.debug("Getting session instance by id: {}", sessionInstanceId);
        try {
            SessionInstance sessionInstance = sessionInstanceService.getSessionInstanceById(sessionInstanceId);
            logger.debug("Found session instance with id: {}", sessionInstanceId);
            return ResponseEntity.ok(SessionInstanceDto.from(sessionInstance));
        } catch (RuntimeException ex) {
            logger.warn("Session instance not found with id: {}", sessionInstanceId);
            throw ex;
        }
    }

    @GetMapping("/user/{subId}")
    public ResponseEntity<Page<SessionInstanceDto>> getSessionInstancesForUser(
            @PathVariable("subId") String subId,
            @PageableDefault(size = 10, sort = "workout_exercise_id", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.debug("Getting session instances for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        Page<SessionInstance> sessionInstances = sessionInstanceService.getSessionInstancesByUserId(subId, pageable);
        logger.debug("Found {} session instances for user: {} (page {} of {})",
                sessionInstances.getNumberOfElements(), subId, sessionInstances.getNumber(), sessionInstances.getTotalPages());
        return ResponseEntity.ok(sessionInstances.map(SessionInstanceDto::from));
    }
}
