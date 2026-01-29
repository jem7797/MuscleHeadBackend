package com.MuscleHead.MuscleHead.Workout.SessionLog;

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
@RequestMapping("sessionLog/api/")
public class SessionLogController {

    private static final Logger logger = LoggerFactory.getLogger(SessionLogController.class);

    @Autowired
    private SessionLogService sessionLogService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<SessionLogResponse> createSessionLog(@Valid @RequestBody SessionLogRequest request) {
        logger.info("Creating new session log");
        
        // Resolve authenticated user
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            logger.error("Attempted to create session log without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(subId)
                .orElseThrow(() -> {
                    logger.error("User not found with sub_id: {}", subId);
                    return new RuntimeException("User not found: " + subId);
                });

        try {
            SessionLog createdSessionLog = sessionLogService.createSessionLog(user, request);
            logger.info("Successfully created session log with id: {} for user: {}",
                    createdSessionLog.getId(), subId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SessionLogResponse(createdSessionLog.getId()));
        } catch (Exception ex) {
            logger.error("Error creating session log for user: {}", subId, ex);
            throw ex;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionLog> updateSessionLog(
            @PathVariable("id") long sessionLogId,
            @Valid @RequestBody SessionLog sessionLog) {
        logger.info("Updating session log with id: {}", sessionLogId);
        sessionLog.setId(sessionLogId);
        return sessionLogService.updateSessionLogById(sessionLogId, sessionLog)
                .map(updatedSessionLog -> {
                    logger.info("Successfully updated session log with id: {}", sessionLogId);
                    return ResponseEntity.ok(updatedSessionLog);
                })
                .orElseGet(() -> {
                    logger.warn("Session log not found for update: id: {}", sessionLogId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSessionLog(@PathVariable("id") long sessionLogId) {
        logger.info("Deleting session log with id: {}", sessionLogId);
        if (sessionLogService.deleteSessionLogById(sessionLogId)) {
            logger.info("Successfully deleted session log with id: {}", sessionLogId);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Session log not found for deletion: id: {}", sessionLogId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionLog> getSessionLogById(@PathVariable("id") long sessionLogId) {
        logger.debug("Getting session log by id: {}", sessionLogId);
        try {
            SessionLog sessionLog = sessionLogService.getSessionLogById(sessionLogId);
            logger.debug("Found session log with id: {}", sessionLogId);
            return ResponseEntity.ok(sessionLog);
        } catch (RuntimeException ex) {
            logger.warn("Session log not found with id: {}", sessionLogId);
            throw ex;
        }
    }

    @GetMapping("/user/{subId}")
    public ResponseEntity<Page<SessionLog>> getSessionLogsForUser(
            @PathVariable("subId") String subId,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.debug("Getting session logs for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        Page<SessionLog> sessionLogs = sessionLogService.getSessionLogsByUserId(subId, pageable);
        logger.debug("Found {} session logs for user: {} (page {} of {})",
                sessionLogs.getNumberOfElements(), subId, sessionLogs.getNumber(), sessionLogs.getTotalPages());
        return ResponseEntity.ok(sessionLogs);
    }
}
