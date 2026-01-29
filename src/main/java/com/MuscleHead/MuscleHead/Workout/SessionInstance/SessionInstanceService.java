package com.MuscleHead.MuscleHead.Workout.SessionInstance;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class SessionInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(SessionInstanceService.class);

    @Autowired
    SessionInstanceRepository sessionInstanceRepository;

    @Autowired
    UserRepository userRepository;

    @Transactional
    public SessionInstance createNewSessionInstance(SessionInstance sessionInstance) {
        logger.debug("Creating new session instance for user: {}",
                sessionInstance != null && sessionInstance.getUser() != null ? sessionInstance.getUser().getSub_id()
                        : "null");
        if (sessionInstance == null || sessionInstance.getUser() == null
                || sessionInstance.getUser().getSub_id() == null) {
            logger.error("Attempted to create session instance with null session instance, user, or sub_id");
            throw new IllegalArgumentException("Error creating new session instance");
        }
        SessionInstance savedSessionInstance = sessionInstanceRepository.save(sessionInstance);
        logger.info("Session instance created successfully with id: {} for user: {}",
                savedSessionInstance.getWorkout_exercise_id(), savedSessionInstance.getUser().getSub_id());

        // Update user's highest weight lifted if session instance's highest lift is
        // greater
        updateUserHighestWeightLifted(savedSessionInstance);

        return savedSessionInstance;
    }

    @Transactional
    public boolean deleteSessionInstance(SessionInstance sessionInstance) {
        if (sessionInstance == null || sessionInstance.getWorkout_exercise_id() == 0) {
            return false;
        }
        if (!sessionInstanceRepository.existsById(sessionInstance.getWorkout_exercise_id())) {
            return false;
        }
        sessionInstanceRepository.delete(sessionInstance);
        return true;
    }

    @Transactional
    public boolean deleteSessionInstanceById(long sessionInstanceId) {
        logger.debug("Deleting session instance with id: {}", sessionInstanceId);
        if (sessionInstanceId == 0) {
            logger.warn("Attempted to delete session instance with invalid id: 0");
            return false;
        }
        if (!sessionInstanceRepository.existsById(sessionInstanceId)) {
            logger.warn("Session instance not found for deletion: id: {}", sessionInstanceId);
            return false;
        }
        sessionInstanceRepository.deleteById(sessionInstanceId);
        logger.info("Session instance deleted successfully with id: {}", sessionInstanceId);
        return true;
    }

    @Transactional
    public boolean updateSessionInstance(SessionInstance updatedSessionInstance) {
        if (updatedSessionInstance == null || updatedSessionInstance.getWorkout_exercise_id() == 0) {
            return false;
        }

        return sessionInstanceRepository.findById(updatedSessionInstance.getWorkout_exercise_id())
                .map(existingSessionInstance -> {
            
                    existingSessionInstance.setMovement(updatedSessionInstance.getMovement());
                    existingSessionInstance.setArea_of_activation(updatedSessionInstance.getArea_of_activation());
                    existingSessionInstance.setReps(updatedSessionInstance.getReps());
                    existingSessionInstance.setSets(updatedSessionInstance.getSets());
                    existingSessionInstance.setDuration(updatedSessionInstance.getDuration());
                    existingSessionInstance.setTotal_weight_lifted(updatedSessionInstance.getTotal_weight_lifted());
                    existingSessionInstance.setWorkout_highest_lift(updatedSessionInstance.getWorkout_highest_lift());
                    existingSessionInstance.setUser(updatedSessionInstance.getUser());
                    existingSessionInstance.setSessionLog(updatedSessionInstance.getSessionLog());

                    SessionInstance savedSessionInstance = sessionInstanceRepository.save(existingSessionInstance);

                    // Update user's highest weight lifted if session instance's highest lift is
                    // greater
                    updateUserHighestWeightLifted(savedSessionInstance);

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public java.util.Optional<SessionInstance> updateSessionInstanceById(long sessionInstanceId,
            SessionInstance updatedSessionInstance) {
        logger.debug("Updating session instance with id: {}", sessionInstanceId);
        if (updatedSessionInstance == null) {
            logger.error("Attempted to update session instance with null session instance object");
            return java.util.Optional.empty();
        }

        return sessionInstanceRepository.findById(sessionInstanceId)
                .map(existingSessionInstance -> {
                    logger.debug("Found existing session instance, updating fields for id: {}", sessionInstanceId);
                   
                    existingSessionInstance.setMovement(updatedSessionInstance.getMovement());
                    existingSessionInstance.setArea_of_activation(updatedSessionInstance.getArea_of_activation());
                    existingSessionInstance.setReps(updatedSessionInstance.getReps());
                    existingSessionInstance.setSets(updatedSessionInstance.getSets());
                    existingSessionInstance.setDuration(updatedSessionInstance.getDuration());
                    existingSessionInstance.setTotal_weight_lifted(updatedSessionInstance.getTotal_weight_lifted());
                    existingSessionInstance.setWorkout_highest_lift(updatedSessionInstance.getWorkout_highest_lift());
                    existingSessionInstance.setUser(updatedSessionInstance.getUser());
                    existingSessionInstance.setSessionLog(updatedSessionInstance.getSessionLog());

                    SessionInstance savedSessionInstance = sessionInstanceRepository.save(existingSessionInstance);
                    logger.info("Session instance updated successfully with id: {}", sessionInstanceId);

                    // Update user's highest weight lifted if session instance's highest lift is
                    // greater
                    updateUserHighestWeightLifted(savedSessionInstance);

                    return savedSessionInstance;
                });
    }

    public SessionInstance getSessionInstanceById(long sessionInstanceId) {
        logger.debug("Getting session instance by id: {}", sessionInstanceId);
        return sessionInstanceRepository.findById(sessionInstanceId)
                .orElseThrow(() -> {
                    logger.warn("Session instance not found with id: {}", sessionInstanceId);
                    return new RuntimeException("Session instance not found: " + sessionInstanceId);
                });
    }

    public List<SessionInstance> getSessionInstancesByUserId(String subId) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return sessionInstanceRepository.findByUser_SubId(subId);
    }

    public Page<SessionInstance> getSessionInstancesByUserId(String subId, Pageable pageable) {
        logger.debug("Getting session instances for user: {} with page: {}, size: {}",
                subId, pageable.getPageNumber(), pageable.getPageSize());
        if (subId == null || subId.isBlank()) {
            logger.error("Attempted to get session instances with null or blank sub_id");
            throw new IllegalArgumentException("User id must not be blank");
        }
        Page<SessionInstance> sessionInstances = sessionInstanceRepository.findByUser_SubId(subId, pageable);
        logger.debug("Found {} session instances for user: {} (total: {})",
                sessionInstances.getNumberOfElements(), subId, sessionInstances.getTotalElements());
        return sessionInstances;
    }

    public List<SessionInstance> getSessionInstancesBySessionId(long sessionId) {
        return sessionInstanceRepository.findByWorkoutSessionId(sessionId);
    }

    /**
     * Compares the session instance's highest lift with the user's highest weight
     * lifted.
     * If the session instance's highest lift is greater, updates the user's highest
     * weight
     * lifted.
     * 
     * @param sessionInstance The session instance to compare
     */
    @Transactional
    private void updateUserHighestWeightLifted(SessionInstance sessionInstance) {
        if (sessionInstance == null || sessionInstance.getUser() == null
                || sessionInstance.getUser().getSub_id() == null) {
            logger.debug("Cannot update user highest weight lifted: session instance or user is null");
            return;
        }

        double sessionHighestLift = sessionInstance.getWorkout_highest_lift();
        if (sessionHighestLift <= 0) {
            logger.debug("Session instance highest lift is 0 or negative, skipping update");
            return;
        }

        Optional<User> userOpt = userRepository.findById(sessionInstance.getUser().getSub_id());
        if (userOpt.isEmpty()) {
            logger.warn("User not found with sub_id: {}", sessionInstance.getUser().getSub_id());
            return;
        }

        User user = userOpt.get();
        double userHighestWeightLifted = user.getHighest_weight_lifted();

        if (sessionHighestLift > userHighestWeightLifted) {
            logger.info("Updating user's highest weight lifted from {} to {} for user: {}",
                    userHighestWeightLifted, sessionHighestLift, user.getSub_id());
            user.setHighest_weight_lifted(sessionHighestLift);
            userRepository.save(user);
        } else {
            logger.debug(
                    "Session instance highest lift ({}) is not greater than user's highest weight lifted ({}), no update needed",
                    sessionHighestLift, userHighestWeightLifted);
        }
    }
}
