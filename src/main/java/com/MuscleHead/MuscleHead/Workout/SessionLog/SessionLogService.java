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

import com.MuscleHead.MuscleHead.Medal.MedalResponse;
import com.MuscleHead.MuscleHead.Medal.MedalService;
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

    @Autowired
    MedalService medalService;

    @Transactional
    public CreateSessionLogResult createSessionLog(User user, SessionLogRequest request) {
        if (user == null || user.getSub_id() == null) {
            throw new IllegalArgumentException("User is required");
        }
        
        if (request == null || request.getExercises() == null || request.getExercises().isEmpty()) {
            throw new IllegalArgumentException("Exercises list is required and cannot be empty");
        }

        // Create SessionLog
        SessionLog sessionLog = new SessionLog();
        sessionLog.setUser(user);
        sessionLog.setDate(Instant.now());
        sessionLog.setNotes(request.getNotes());
        if (request.getTimeSpentInGym() != null) {
            sessionLog.setTimeSpentInGym(request.getTimeSpentInGym());
        }
        
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
        user.setLifetime_weight_lifted(user.getLifetime_weight_lifted() + totalWeightLifted);
        if (sessionHighestLift > user.getHighest_weight_lifted()) {
            user.setHighest_weight_lifted(sessionHighestLift);
        }
        int gymTime = request.getTimeSpentInGym() != null ? request.getTimeSpentInGym() : 0;
        user.setLifetime_gym_time(user.getLifetime_gym_time() + gymTime);
        userRepository.save(user);
        userService.levelUp(user);
        List<MedalResponse> newlyAwarded = medalService.checkAndAwardMedals(user, savedSessionLog);

        return new CreateSessionLogResult(savedSessionLog, newlyAwarded);
    }

    @Transactional
    public SessionLog createNewSessionLog(SessionLog sessionLog) {
        if (sessionLog == null || sessionLog.getUser() == null || sessionLog.getUser().getSub_id() == null) {
            throw new IllegalArgumentException("Error creating new session log");
        }
        SessionLog savedSessionLog = sessionLogRepository.save(sessionLog);

        User user = savedSessionLog.getUser();
        user.setXP(user.getXP() + 1);
        double sessionWeight = savedSessionLog.getTotal_weight_lifted() != null ? savedSessionLog.getTotal_weight_lifted() : 0;
        double sessionHighest = savedSessionLog.getSession_highest_lift() != null ? savedSessionLog.getSession_highest_lift() : 0;
        user.setLifetime_weight_lifted(user.getLifetime_weight_lifted() + sessionWeight);
        if (sessionHighest > user.getHighest_weight_lifted()) {
            user.setHighest_weight_lifted(sessionHighest);
        }
        user.setLifetime_gym_time(user.getLifetime_gym_time() + savedSessionLog.getTimeSpentInGym());
        userRepository.save(user);
        userService.levelUp(user);
        medalService.checkAndAwardMedals(user, savedSessionLog);

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
        if (sessionLogId == 0) {
            logger.warn("Attempted to delete session log with invalid id: 0");
            return false;
        }
        return sessionLogRepository.findById(sessionLogId)
                .map(sessionLog -> {
                    User user = sessionLog.getUser();
                    sessionLogRepository.delete(sessionLog);
                    medalService.checkDeleteMedal(user);
                    return true;
                })
                .orElseGet(() -> {
                    logger.warn("Session log not found for deletion: id: {}", sessionLogId);
                    return false;
                });
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
                    existingSessionLog.setTimeSpentInGym(updatedSessionLog.getTimeSpentInGym());
                    existingSessionLog.setUser(updatedSessionLog.getUser());

                    SessionLog savedSessionLog = sessionLogRepository.save(existingSessionLog);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public Optional<SessionLog> updateSessionLogById(long sessionLogId, SessionLog updatedSessionLog) {
        if (updatedSessionLog == null) {
            return Optional.empty();
        }

        return sessionLogRepository.findById(sessionLogId)
                .map(existingSessionLog -> {
                    existingSessionLog.setNotes(updatedSessionLog.getNotes());
                    existingSessionLog.setRoutine(updatedSessionLog.getRoutine());
                    existingSessionLog.setTotal_weight_lifted(updatedSessionLog.getTotal_weight_lifted());
                    existingSessionLog.setSession_highest_lift(updatedSessionLog.getSession_highest_lift());
                    existingSessionLog.setTotal_duration(updatedSessionLog.getTotal_duration());
                    existingSessionLog.setTimeSpentInGym(updatedSessionLog.getTimeSpentInGym());
                    existingSessionLog.setUser(updatedSessionLog.getUser());

                    return sessionLogRepository.save(existingSessionLog);
                });
    }

    public SessionLog getSessionLogById(long sessionLogId) {
        return sessionLogRepository.findById(sessionLogId)
                .orElseThrow(() -> new RuntimeException("Session log not found: " + sessionLogId));
    }

    public List<SessionLog> getSessionLogsByUserId(String subId) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return sessionLogRepository.findByUser_SubId(subId);
    }

    public Page<SessionLog> getSessionLogsByUserId(String subId, Pageable pageable) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return sessionLogRepository.findByUser_SubId(subId, pageable);
    }

    /**
     * Returns the max lift for a workout. Computes from session instances if
     * session_highest_lift is null, and stores it on the session.
     */
    @Transactional
    public double getMaxLiftAndStore(long sessionLogId) {
        SessionLog sessionLog = getSessionLogById(sessionLogId);
        double maxFromInstances = sessionInstanceRepository.findMaxLiftBySessionId(sessionLogId);
        Double stored = sessionLog.getSession_highest_lift();
        if (stored == null || maxFromInstances > stored) {
            sessionLog.setSession_highest_lift(maxFromInstances);
            sessionLogRepository.save(sessionLog);
        }
        return stored != null && stored > maxFromInstances ? stored : maxFromInstances;
    }

    /**
     * Syncs max lift for all of a user's workouts by computing from session
     * instances and storing on each SessionLog.
     */
    @Transactional
    public int syncMaxLiftsForUser(String subId) {
        List<SessionLog> sessions = sessionLogRepository.findByUser_SubId(subId);
        int updated = 0;
        for (SessionLog session : sessions) {
            double maxFromInstances = sessionInstanceRepository.findMaxLiftBySessionId(session.getId());
            Double stored = session.getSession_highest_lift();
            if (stored == null || maxFromInstances > (stored != null ? stored : 0)) {
                session.setSession_highest_lift(maxFromInstances);
                sessionLogRepository.save(session);
                updated++;
            }
        }
        return updated;
    }
}


