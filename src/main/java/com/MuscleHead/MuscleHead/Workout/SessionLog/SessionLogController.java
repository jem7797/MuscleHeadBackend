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
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(subId)
                .orElseThrow(() -> new RuntimeException("User not found: " + subId));

        CreateSessionLogResult result = sessionLogService.createSessionLog(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SessionLogResponse(result.getSessionLog().getId(), result.getNewlyAwardedMedals()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionLog> updateSessionLog(
            @PathVariable("id") long sessionLogId,
            @Valid @RequestBody SessionLog sessionLog) {
        sessionLog.setId(sessionLogId);
        return sessionLogService.updateSessionLogById(sessionLogId, sessionLog)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Session log not found for update: id: {}", sessionLogId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSessionLog(@PathVariable("id") long sessionLogId) {
        if (sessionLogService.deleteSessionLogById(sessionLogId)) {
            return ResponseEntity.noContent().build();
        }
        logger.warn("Session log not found for deletion: id: {}", sessionLogId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{subId}")
    public ResponseEntity<Page<SessionLog>> getSessionLogsForUser(
            @PathVariable("subId") String subId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SessionLog> sessionLogs = sessionLogService.getSessionLogsByUserId(subId, pageable);
        return ResponseEntity.ok(sessionLogs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionLog> getSessionLogById(@PathVariable("id") long sessionLogId) {
        SessionLog sessionLog = sessionLogService.getSessionLogById(sessionLogId);
        return ResponseEntity.ok(sessionLog);
    }

    @GetMapping("/{id}/max-lift")
    public ResponseEntity<MaxLiftResponse> getMaxLift(@PathVariable("id") long sessionLogId) {
        double maxLift = sessionLogService.getMaxLiftAndStore(sessionLogId);
        return ResponseEntity.ok(new MaxLiftResponse(sessionLogId, maxLift));
    }

    @GetMapping("/user/{subId}/sync-max-lifts")
    public ResponseEntity<SyncMaxLiftsResponse> syncMaxLifts(@PathVariable("subId") String subId) {
        String authSubId = SecurityUtils.getCurrentUserSub();
        if (authSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authSubId.equals(subId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        int updated = sessionLogService.syncMaxLiftsForUser(subId);
        return ResponseEntity.ok(new SyncMaxLiftsResponse(updated));
    }
}
