package com.MuscleHead.MuscleHead.Workout.SessionLog;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.Movement.Movement;
import com.MuscleHead.MuscleHead.Movement.MovementRepository;
import com.MuscleHead.MuscleHead.Routine.WorkoutTemplate.WorkoutTemplate;
import com.MuscleHead.MuscleHead.Routine.WorkoutTemplate.WorkoutTemplateRepository;
import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.User.UserService;
import com.MuscleHead.MuscleHead.Workout.SessionInstance.SessionInstance;
import com.MuscleHead.MuscleHead.Workout.SessionInstance.SessionInstanceRepository;
import com.MuscleHead.MuscleHead.Workout.SessionInstance.SessionInstanceRequest;

import jakarta.transaction.Transactional;

@Service
public class SessionLogService {

    private static final Logger logger = LoggerFactory.getLogger(SessionLogService.class);

    @Autowired
    SessionLogRepository sessionLogRepository;

    @Autowired
    SessionInstanceRepository sessionInstanceRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    MovementRepository exerciseRepository;

    @Autowired
    WorkoutTemplateRepository workoutTemplateRepository;

    @Transactional
    public SessionLog createSessionLog(User user, SessionLogRequest request) {
        logger.debug("Creating new session log for user: {}", user != null ? user.getSub_id() : "null");
        
        if (user == null || user.getSub_id() == null) {
            logger.error("Attempted to create session log with null user or sub_id");
            throw new IllegalArgumentException("User is required");
        }
        
        if (request == null || request.getExercises() == null || request.getExercises().isEmpty()) {
            logger.error("Attempted to create session log with null or empty exercises list");
            throw new IllegalArgumentException("Exercises list is required and cannot be empty");
        }

        // Create SessionLog
        SessionLog sessionLog = new SessionLog();
        sessionLog.setUser(user);
        sessionLog.setDate(Instant.now());
        sessionLog.setNotes(request.getNotes());
        
        // Set routine if provided
        if (request.getRoutineId() != null) {
            WorkoutTemplate routine = workoutTemplateRepository.findById(request.getRoutineId())
                    .orElseThrow(() -> {
                        logger.warn("Routine not found with id: {}", request.getRoutineId());
                        return new IllegalArgumentException("Routine not found: " + request.getRoutineId());
                    });
            sessionLog.setRoutine(routine);
        }

        // Create SessionInstance entities
        List<SessionInstance> sessionInstances = new ArrayList<>();
        double totalWeightLifted = 0.0;
        double sessionHighestLift = 0.0;
        double totalDuration = 0.0;

        for (SessionInstanceRequest exerciseRequest : request.getExercises()) {
            // Find Exercise
            Movement exercise = exerciseRepository.findById(exerciseRequest.getExerciseId())
                    .orElseThrow(() -> {
                        logger.warn("Exercise not found with id: {}", exerciseRequest.getExerciseId());
                        return new IllegalArgumentException("Exercise not found: " + exerciseRequest.getExerciseId());
                    });

            // Create SessionInstance
            SessionInstance sessionInstance = new SessionInstance();
            sessionInstance.setUser(user);
            sessionInstance.setSessionLog(sessionLog);
            sessionInstance.setMovement(exercise);
            sessionInstance.setSets(exerciseRequest.getSets());
            sessionInstance.setReps(exerciseRequest.getReps());
            
            // Calculate weight-related fields
            double weight = exerciseRequest.getWeight();
            double exerciseTotalWeight = weight * exerciseRequest.getSets() * exerciseRequest.getReps();
            sessionInstance.setWorkout_highest_lift(weight);
            sessionInstance.setTotal_weight_lifted(exerciseTotalWeight);
            
            // Update session aggregates
            totalWeightLifted += exerciseTotalWeight;
            if (weight > sessionHighestLift) {
                sessionHighestLift = weight;
            }
            // Duration is optional - assume 0 if not provided in DTO

            sessionInstances.add(sessionInstance);
        }

        // Set aggregates on session
        sessionLog.setTotal_weight_lifted(totalWeightLifted);
        sessionLog.setSession_highest_lift(sessionHighestLift);
        sessionLog.setTotal_duration(totalDuration);
        sessionLog.setSessionInstances(sessionInstances);

        // Save session log (cascade will save exercises)
        SessionLog savedSessionLog = sessionLogRepository.save(sessionLog);

        user.setXP(user.getXP() + 1);
        userRepository.save(user);
        userService.levelUp(user);

        logger.info("Session log created successfully with id: {} for user: {} with {} exercises",
                savedSessionLog.getId(), savedSessionLog.getUser().getSub_id(), sessionInstances.size());

        return savedSessionLog;
    }

    @Transactional
    public SessionLog createNewSessionLog(SessionLog sessionLog) {
        logger.debug("Creating new session log for user: {}",
                sessionLog != null && sessionLog.getUser() != null ? sessionLog.getUser().getSub_id() : "null");
        if (sessionLog == null || sessionLog.getUser() == null || sessionLog.getUser().getSub_id() == null) {
            logger.error("Attempted to create session log with null session log, user, or sub_id");
            throw new IllegalArgumentException("Error creating new session log");
        }
        SessionLog savedSessionLog = sessionLogRepository.save(sessionLog);

        User user = savedSessionLog.getUser();
        user.setXP(user.getXP() + 1);
        userRepository.save(user);
        userService.levelUp(user);

        logger.info("Session log created successfully with id: {} for user: {}",
                savedSessionLog.getId(), savedSessionLog.getUser().getSub_id());
        return savedSessionLog;
    }

    @Transactional
    public boolean deleteSessionLog(SessionLog sessionLog) {
        if (sessionLog == null || sessionLog.getId() == 0) {
            return false;
        }
        if (!sessionLogRepository.existsById(sessionLog.getId())) {
            return false;
        }
        sessionLogRepository.delete(sessionLog);
        return true;
    }

    @Transactional
    public boolean deleteSessionLogById(long sessionLogId) {
        logger.debug("Deleting session log with id: {}", sessionLogId);
        if (sessionLogId == 0) {
            logger.warn("Attempted to delete session log with invalid id: 0");
            return false;
        }
        if (!sessionLogRepository.existsById(sessionLogId)) {
            logger.warn("Session log not found for deletion: id: {}", sessionLogId);
            return false;
        }
        sessionLogRepository.deleteById(sessionLogId);
        logger.info("Session log deleted successfully with id: {}", sessionLogId);
        return true;
    }

    @Transactional
    public boolean updateSessionLog(SessionLog updatedSessionLog) {
        if (updatedSessionLog == null || updatedSessionLog.getId() == 0) {
            return false;
        }

        return sessionLogRepository.findById(updatedSessionLog.getId())
                .map(existingSessionLog -> {
                    existingSessionLog.setNotes(updatedSessionLog.getNotes());
                    existingSessionLog.setRoutine(updatedSessionLog.getRoutine());
                    existingSessionLog.setTotal_weight_lifted(updatedSessionLog.getTotal_weight_lifted());
                    existingSessionLog.setSession_highest_lift(updatedSessionLog.getSession_highest_lift());
                    existingSessionLog.setTotal_duration(updatedSessionLog.getTotal_duration());
                    existingSessionLog.setUser(updatedSessionLog.getUser());

                    SessionLog savedSessionLog = sessionLogRepository.save(existingSessionLog);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public Optional<SessionLog> updateSessionLogById(long sessionLogId, SessionLog updatedSessionLog) {
        logger.debug("Updating session log with id: {}", sessionLogId);
        if (updatedSessionLog == null) {
            logger.error("Attempted to update session log with null session log object");
            return Optional.empty();
        }

        return sessionLogRepository.findById(sessionLogId)
                .map(existingSessionLog -> {
                    logger.debug("Found existing session log, updating fields for id: {}", sessionLogId);
                    existingSessionLog.setNotes(updatedSessionLog.getNotes());
                    existingSessionLog.setRoutine(updatedSessionLog.getRoutine());
                    existingSessionLog.setTotal_weight_lifted(updatedSessionLog.getTotal_weight_lifted());
                    existingSessionLog.setSession_highest_lift(updatedSessionLog.getSession_highest_lift());
                    existingSessionLog.setTotal_duration(updatedSessionLog.getTotal_duration());
                    existingSessionLog.setUser(updatedSessionLog.getUser());

                    SessionLog savedSessionLog = sessionLogRepository.save(existingSessionLog);
                    logger.info("Session log updated successfully with id: {}", sessionLogId);
                    return savedSessionLog;
                });
    }

    public SessionLog getSessionLogById(long sessionLogId) {
        logger.debug("Getting session log by id: {}", sessionLogId);
        return sessionLogRepository.findById(sessionLogId)
                .orElseThrow(() -> {
                    logger.warn("Session log not found with id: {}", sessionLogId);
                    return new RuntimeException("Session log not found: " + sessionLogId);
                });
    }

    public List<SessionLog> getSessionLogsByUserId(String subId) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return sessionLogRepository.findByUser_SubId(subId);
    }

    public Page<SessionLog> getSessionLogsByUserId(String subId, Pageable pageable) {
        logger.debug("Getting session logs for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        if (subId == null || subId.isBlank()) {
            logger.error("Attempted to get session logs with null or blank sub_id");
            throw new IllegalArgumentException("User id must not be blank");
        }
        Page<SessionLog> sessionLogs = sessionLogRepository.findByUser_SubId(subId, pageable);
        logger.debug("Found {} session logs for user: {} (total: {})",
                sessionLogs.getNumberOfElements(), subId, sessionLogs.getTotalElements());
        return sessionLogs;
    }
}


