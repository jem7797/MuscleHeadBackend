package com.MuscleHead.MuscleHead.Workout;

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
import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.WorkoutTemplate.WorkoutTemplate;
import com.MuscleHead.MuscleHead.WorkoutTemplate.WorkoutTemplateRepository;

import jakarta.transaction.Transactional;

@Service
public class WorkoutSessionService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutSessionService.class);

    @Autowired
    WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MovementRepository exerciseRepository;

    @Autowired
    WorkoutTemplateRepository routineRepository;

    @Transactional
    public WorkoutSession createWorkoutSession(User user, WorkoutSessionRequest request) {
        logger.debug("Creating new workout session for user: {}", user != null ? user.getSub_id() : "null");
        
        if (user == null || user.getSub_id() == null) {
            logger.error("Attempted to create workout session with null user or sub_id");
            throw new IllegalArgumentException("User is required");
        }
        
        if (request == null || request.getExercises() == null || request.getExercises().isEmpty()) {
            logger.error("Attempted to create workout session with null or empty exercises list");
            throw new IllegalArgumentException("Exercises list is required and cannot be empty");
        }

        // Create WorkoutSession
        WorkoutSession session = new WorkoutSession();
        session.setUser(user);
        session.setDate(Instant.now());
        session.setNotes(request.getNotes());
        
        // Set routine if provided
        if (request.getRoutineId() != null) {
            WorkoutTemplate routine = routineRepository.findById(request.getRoutineId())
                    .orElseThrow(() -> {
                        logger.warn("Routine not found with id: {}", request.getRoutineId());
                        return new IllegalArgumentException("Routine not found: " + request.getRoutineId());
                    });
            session.setRoutine(routine);
        }

        // Create WorkoutExercise entities
        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        double totalWeightLifted = 0.0;
        double sessionHighestLift = 0.0;
        double totalDuration = 0.0;

        for (WorkoutExerciseRequest exerciseRequest : request.getExercises()) {
            // Find Exercise
            Movement exercise = exerciseRepository.findById(exerciseRequest.getExerciseId())
                    .orElseThrow(() -> {
                        logger.warn("Exercise not found with id: {}", exerciseRequest.getExerciseId());
                        return new IllegalArgumentException("Exercise not found: " + exerciseRequest.getExerciseId());
                    });

            // Create WorkoutExercise
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setUser(user);
            workoutExercise.setWorkoutSession(session);
            workoutExercise.setExercise(exercise);
            workoutExercise.setSets(exerciseRequest.getSets());
            workoutExercise.setReps(exerciseRequest.getReps());
            workoutExercise.setNotes(exerciseRequest.getNotes());
            
            // Calculate weight-related fields
            double weight = exerciseRequest.getWeight();
            double exerciseTotalWeight = weight * exerciseRequest.getSets() * exerciseRequest.getReps();
            workoutExercise.setWorkout_highest_lift(weight);
            workoutExercise.setTotal_weight_lifted(exerciseTotalWeight);
            
            // Update session aggregates
            totalWeightLifted += exerciseTotalWeight;
            if (weight > sessionHighestLift) {
                sessionHighestLift = weight;
            }
            // Duration is optional - assume 0 if not provided in DTO

            workoutExercises.add(workoutExercise);
        }

        // Set aggregates on session
        session.setTotal_weight_lifted(totalWeightLifted);
        session.setSession_highest_lift(sessionHighestLift);
        session.setTotal_duration(totalDuration);
        session.setWorkoutExercises(workoutExercises);

        // Save session (cascade will save exercises)
        WorkoutSession savedSession = workoutSessionRepository.save(session);
        logger.info("Workout session created successfully with id: {} for user: {} with {} exercises",
                savedSession.getId(), savedSession.getUser().getSub_id(), workoutExercises.size());
        
        return savedSession;
    }

    @Transactional
    public WorkoutSession createNewWorkoutSession(WorkoutSession workoutSession) {
        logger.debug("Creating new workout session for user: {}",
                workoutSession != null && workoutSession.getUser() != null ? workoutSession.getUser().getSub_id() : "null");
        if (workoutSession == null || workoutSession.getUser() == null || workoutSession.getUser().getSub_id() == null) {
            logger.error("Attempted to create workout session with null workout session, user, or sub_id");
            throw new IllegalArgumentException("Error creating new workout session");
        }
        WorkoutSession savedWorkoutSession = workoutSessionRepository.save(workoutSession);
        logger.info("Workout session created successfully with id: {} for user: {}",
                savedWorkoutSession.getId(), savedWorkoutSession.getUser().getSub_id());
        return savedWorkoutSession;
    }

    @Transactional
    public boolean deleteWorkoutSession(WorkoutSession workoutSession) {
        if (workoutSession == null || workoutSession.getId() == 0) {
            return false;
        }
        if (!workoutSessionRepository.existsById(workoutSession.getId())) {
            return false;
        }
        workoutSessionRepository.delete(workoutSession);
        return true;
    }

    @Transactional
    public boolean deleteWorkoutSessionById(long workoutSessionId) {
        logger.debug("Deleting workout session with id: {}", workoutSessionId);
        if (workoutSessionId == 0) {
            logger.warn("Attempted to delete workout session with invalid id: 0");
            return false;
        }
        if (!workoutSessionRepository.existsById(workoutSessionId)) {
            logger.warn("Workout session not found for deletion: id: {}", workoutSessionId);
            return false;
        }
        workoutSessionRepository.deleteById(workoutSessionId);
        logger.info("Workout session deleted successfully with id: {}", workoutSessionId);
        return true;
    }

    @Transactional
    public boolean updateWorkoutSession(WorkoutSession updatedWorkoutSession) {
        if (updatedWorkoutSession == null || updatedWorkoutSession.getId() == 0) {
            return false;
        }

        return workoutSessionRepository.findById(updatedWorkoutSession.getId())
                .map(existingWorkoutSession -> {
                    existingWorkoutSession.setNotes(updatedWorkoutSession.getNotes());
                    existingWorkoutSession.setRoutine(updatedWorkoutSession.getRoutine());
                    existingWorkoutSession.setTotal_weight_lifted(updatedWorkoutSession.getTotal_weight_lifted());
                    existingWorkoutSession.setSession_highest_lift(updatedWorkoutSession.getSession_highest_lift());
                    existingWorkoutSession.setTotal_duration(updatedWorkoutSession.getTotal_duration());
                    existingWorkoutSession.setUser(updatedWorkoutSession.getUser());

                    WorkoutSession savedWorkoutSession = workoutSessionRepository.save(existingWorkoutSession);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public Optional<WorkoutSession> updateWorkoutSessionById(long workoutSessionId, WorkoutSession updatedWorkoutSession) {
        logger.debug("Updating workout session with id: {}", workoutSessionId);
        if (updatedWorkoutSession == null) {
            logger.error("Attempted to update workout session with null workout session object");
            return Optional.empty();
        }

        return workoutSessionRepository.findById(workoutSessionId)
                .map(existingWorkoutSession -> {
                    logger.debug("Found existing workout session, updating fields for id: {}", workoutSessionId);
                    existingWorkoutSession.setNotes(updatedWorkoutSession.getNotes());
                    existingWorkoutSession.setRoutine(updatedWorkoutSession.getRoutine());
                    existingWorkoutSession.setTotal_weight_lifted(updatedWorkoutSession.getTotal_weight_lifted());
                    existingWorkoutSession.setSession_highest_lift(updatedWorkoutSession.getSession_highest_lift());
                    existingWorkoutSession.setTotal_duration(updatedWorkoutSession.getTotal_duration());
                    existingWorkoutSession.setUser(updatedWorkoutSession.getUser());

                    WorkoutSession savedWorkoutSession = workoutSessionRepository.save(existingWorkoutSession);
                    logger.info("Workout session updated successfully with id: {}", workoutSessionId);
                    return savedWorkoutSession;
                });
    }

    public WorkoutSession getWorkoutSessionById(long workoutSessionId) {
        logger.debug("Getting workout session by id: {}", workoutSessionId);
        return workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> {
                    logger.warn("Workout session not found with id: {}", workoutSessionId);
                    return new RuntimeException("Workout session not found: " + workoutSessionId);
                });
    }

    public List<WorkoutSession> getWorkoutSessionsByUserId(String subId) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return workoutSessionRepository.findByUser_SubId(subId);
    }

    public Page<WorkoutSession> getWorkoutSessionsByUserId(String subId, Pageable pageable) {
        logger.debug("Getting workout sessions for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        if (subId == null || subId.isBlank()) {
            logger.error("Attempted to get workout sessions with null or blank sub_id");
            throw new IllegalArgumentException("User id must not be blank");
        }
        Page<WorkoutSession> workoutSessions = workoutSessionRepository.findByUser_SubId(subId, pageable);
        logger.debug("Found {} workout sessions for user: {} (total: {})",
                workoutSessions.getNumberOfElements(), subId, workoutSessions.getTotalElements());
        return workoutSessions;
    }
}
