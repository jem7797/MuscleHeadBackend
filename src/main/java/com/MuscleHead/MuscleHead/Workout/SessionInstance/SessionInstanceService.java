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
        if (sessionInstance == null || sessionInstance.getUser() == null
                || sessionInstance.getUser().getSub_id() == null) {
            throw new IllegalArgumentException("Error creating new session instance");
        }
        SessionInstance savedSessionInstance = sessionInstanceRepository.save(sessionInstance);
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
        if (sessionInstanceId == 0) {
            logger.warn("Attempted to delete session instance with invalid id: 0");
            return false;
        }
        if (!sessionInstanceRepository.existsById(sessionInstanceId)) {
            logger.warn("Session instance not found for deletion: id: {}", sessionInstanceId);
            return false;
        }
        sessionInstanceRepository.deleteById(sessionInstanceId);
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
                    updateUserHighestWeightLifted(savedSessionInstance);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public java.util.Optional<SessionInstance> updateSessionInstanceById(long sessionInstanceId,
            SessionInstance updatedSessionInstance) {
        if (updatedSessionInstance == null) {
            return java.util.Optional.empty();
        }

        return sessionInstanceRepository.findById(sessionInstanceId)
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
                    updateUserHighestWeightLifted(savedSessionInstance);
                    return savedSessionInstance;
                });
    }

    public SessionInstance getSessionInstanceById(long sessionInstanceId) {
        return sessionInstanceRepository.findByIdWithMovement(sessionInstanceId)
                .orElseThrow(() -> new RuntimeException("Session instance not found: " + sessionInstanceId));
    }

    public List<SessionInstance> getSessionInstancesByUserId(String subId) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return sessionInstanceRepository.findByUser_SubId(subId);
    }

    public Page<SessionInstance> getSessionInstancesByUserId(String subId, Pageable pageable) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return sessionInstanceRepository.findByUser_SubId(subId, pageable);
    }

    public List<SessionInstance> getSessionInstancesBySessionId(long sessionId) {
        return sessionInstanceRepository.findByWorkoutSessionId(sessionId);
    }

    @Transactional
    private void updateUserHighestWeightLifted(SessionInstance sessionInstance) {
        if (sessionInstance == null || sessionInstance.getUser() == null
                || sessionInstance.getUser().getSub_id() == null) {
            return;
        }

        double sessionHighestLift = sessionInstance.getWorkout_highest_lift();
        if (sessionHighestLift <= 0) {
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
            user.setHighest_weight_lifted(sessionHighestLift);
            userRepository.save(user);
        }
    }
}
