package com.MuscleHead.MuscleHead.Routine;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class RoutineService {

    private static final Logger logger = LoggerFactory.getLogger(RoutineService.class);

    @Autowired
    private RoutineRepository routineRepository;

    @Transactional
    public Routine createNewRoutine(Routine routine) {
        logger.debug("Creating new routine: {}", routine != null ? routine.getName() : "null");
        if (routine == null || routine.getUser() == null) {
            logger.error("Attempted to create routine with null routine or user");
            throw new IllegalArgumentException("Routine and user must exist and not be null");
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
                    if (updatedRoutine.getExercises() != null) {
                        existingRoutine.setExercises(updatedRoutine.getExercises());
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