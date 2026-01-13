package com.MuscleHead.MuscleHead.Routine;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class RoutineService {

    private static final Logger logger = LoggerFactory.getLogger(RoutineService.class);

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private RoutineExerciseRepository routineExerciseRepository;

    @Transactional
    public Routine createNewRoutine(Routine routine) {
        logger.debug("Creating new routine: {}", routine != null ? routine.getName() : "null");
        if (routine == null || routine.getUser() == null) {
            logger.error("Attempted to create routine with null routine or user");
            throw new IllegalArgumentException("Routine and user must exist and not be null");
        }

        // Ensure user exists
        User user = routine.getUser();
        if (user.getSub_id() == null) {
            logger.error("Attempted to create routine with user missing sub_id");
            throw new IllegalArgumentException("User sub_id must not be null");
        }
        User existingUser = userRepository.findById(user.getSub_id())
                .orElseThrow(() -> {
                    logger.error("User not found with sub_id: {}", user.getSub_id());
                    return new IllegalArgumentException("User not found with sub_id: " + user.getSub_id());
                });
        routine.setUser(existingUser);

        // Set routine reference on all RoutineExercises and ensure exercises exist
        if (routine.getRoutineExercises() != null) {
            for (RoutineExercise routineExercise : routine.getRoutineExercises()) {
                routineExercise.setRoutine(routine);
                if (routineExercise.getExercise() != null && routineExercise.getExercise().getId() != null) {
                    // Exercise already exists, fetch it
                    Exercise exercise = exerciseRepository.findById(routineExercise.getExercise().getId())
                            .orElseThrow(() -> {
                                logger.error("Exercise not found with id: {}", routineExercise.getExercise().getId());
                                return new IllegalArgumentException(
                                        "Exercise not found with id: " + routineExercise.getExercise().getId());
                            });
                    routineExercise.setExercise(exercise);
                } else if (routineExercise.getExercise() != null && routineExercise.getExercise().getName() != null) {
                    // Try to find by name, or create new exercise
                    Exercise exercise = exerciseRepository.findByName(routineExercise.getExercise().getName())
                            .orElseGet(() -> {
                                logger.debug("Creating new exercise: {}", routineExercise.getExercise().getName());
                                return exerciseRepository.save(routineExercise.getExercise());
                            });
                    routineExercise.setExercise(exercise);
                }
            }
        }

        Routine savedRoutine = routineRepository.save(routine);
        logger.info("Routine created successfully with id: {}", savedRoutine.getId());
        return savedRoutine;
    }

    @Transactional
    public Optional<Routine> updateRoutine(Routine updatedRoutine) {
        logger.debug("Updating routine with id: {}", updatedRoutine != null ? updatedRoutine.getId() : "null");
        if (updatedRoutine == null || updatedRoutine.getId() == null) {
            logger.error("Attempted to update routine with null routine or id");
            throw new IllegalArgumentException("Routine and id must not be null");
        }

        return routineRepository.findById(updatedRoutine.getId())
                .map(existingRoutine -> {
                    logger.debug("Found existing routine, updating fields for id: {}", updatedRoutine.getId());
                    if (updatedRoutine.getName() != null) {
                        existingRoutine.setName(updatedRoutine.getName());
                    }

                    // Handle RoutineExercises update
                    if (updatedRoutine.getRoutineExercises() != null) {
                        // Remove existing RoutineExercises (orphanRemoval will handle deletion)
                        existingRoutine.getRoutineExercises().clear();

                        // Add new RoutineExercises
                        for (RoutineExercise routineExercise : updatedRoutine.getRoutineExercises()) {
                            routineExercise.setRoutine(existingRoutine);
                            if (routineExercise.getExercise() != null
                                    && routineExercise.getExercise().getId() != null) {
                                // Exercise already exists, fetch it
                                Exercise exercise = exerciseRepository.findById(routineExercise.getExercise().getId())
                                        .orElseThrow(() -> {
                                            logger.error("Exercise not found with id: {}",
                                                    routineExercise.getExercise().getId());
                                            return new IllegalArgumentException("Exercise not found with id: "
                                                    + routineExercise.getExercise().getId());
                                        });
                                routineExercise.setExercise(exercise);
                            } else if (routineExercise.getExercise() != null
                                    && routineExercise.getExercise().getName() != null) {
                                // Try to find by name, or create new exercise
                                Exercise exercise = exerciseRepository
                                        .findByName(routineExercise.getExercise().getName())
                                        .orElseGet(() -> {
                                            logger.debug("Creating new exercise: {}",
                                                    routineExercise.getExercise().getName());
                                            return exerciseRepository.save(routineExercise.getExercise());
                                        });
                                routineExercise.setExercise(exercise);
                            }
                            existingRoutine.getRoutineExercises().add(routineExercise);
                        }
                    }

                    Routine savedRoutine = routineRepository.save(existingRoutine);
                    logger.info("Routine updated successfully with id: {}", savedRoutine.getId());
                    return savedRoutine;
                });
    }

    @Transactional
    public boolean deleteRoutine(Long routineId) {
        logger.debug("Deleting routine with id: {}", routineId);
        if (routineId == null) {
            logger.warn("Attempted to delete routine with null id");
            return false;
        }
        if (!routineRepository.existsById(routineId)) {
            logger.warn("Routine not found for deletion: id: {}", routineId);
            return false;
        }
        routineRepository.deleteById(routineId);
        logger.info("Routine deleted successfully with id: {}", routineId);
        return true;
    }

    public Optional<Routine> getRoutineById(Long routineId) {
        logger.debug("Getting routine by id: {}", routineId);
        if (routineId == null) {
            logger.error("Attempted to get routine with null id");
            throw new IllegalArgumentException("Routine id must not be null");
        }
        Optional<Routine> routine = routineRepository.findById(routineId);
        if (routine.isPresent()) {
            logger.debug("Found routine with id: {}", routineId);
        } else {
            logger.debug("Routine not found with id: {}", routineId);
        }
        return routine;
    }

    public List<Routine> getRoutinesByUserSubId(String subId) {
        logger.debug("Getting routines for user with sub_id: {}", subId);
        if (subId == null || subId.isBlank()) {
            logger.error("Attempted to get routines with null or blank sub_id");
            throw new IllegalArgumentException("User sub_id must not be blank");
        }
        List<Routine> routines = routineRepository.findByUserSub_id(subId);
        logger.debug("Found {} routines for user with sub_id: {}", routines.size(), subId);
        return routines;
    }

    public List<Routine> getAllRoutines() {
        logger.debug("Getting all routines");
        return routineRepository.findAll();
    }
}