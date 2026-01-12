package com.MuscleHead.MuscleHead.Routine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("routine/api/")
@Validated
public class RoutineController {

    private static final Logger logger = LoggerFactory.getLogger(RoutineController.class);

    @Autowired
    private RoutineService routineService;

    @PostMapping
    public ResponseEntity<Routine> createRoutine(@Valid @RequestBody Routine routine) {
        logger.info("Creating new routine: {}", routine.getName());
        try {
            Routine createdRoutine = routineService.createNewRoutine(routine);
            logger.info("Successfully created routine with id: {}", createdRoutine.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRoutine);
        } catch (IllegalArgumentException ex) {
            logger.warn("Failed to create routine: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            logger.error("Error creating routine: {}", routine.getName(), ex);
            throw ex;
        }
    }

    @GetMapping("/{routineId}")
    public ResponseEntity<Routine> getRoutineById(@PathVariable Long routineId) {
        logger.debug("Getting routine by id: {}", routineId);
        return routineService.getRoutineById(routineId)
                .map(routine -> {
                    logger.debug("Found routine with id: {}", routineId);
                    return ResponseEntity.ok(routine);
                })
                .orElseGet(() -> {
                    logger.warn("Routine not found with id: {}", routineId);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<List<Routine>> getRoutines(@RequestParam(required = false) String subId) {
        if (subId != null && !subId.isBlank()) {
            logger.debug("Getting routines for user with sub_id: {}", subId);
            List<Routine> routines = routineService.getRoutinesByUserSubId(subId);
            return ResponseEntity.ok(routines);
        }
        logger.debug("Getting all routines");
        List<Routine> routines = routineService.getAllRoutines();
        return ResponseEntity.ok(routines);
    }

    @PutMapping("/{routineId}")
    public ResponseEntity<Routine> updateRoutine(
            @PathVariable Long routineId,
            @Valid @RequestBody Routine routine) {
        logger.info("Updating routine with id: {}", routineId);
        routine.setId(routineId);
        return routineService.updateRoutine(routine)
                .map(updatedRoutine -> {
                    logger.info("Successfully updated routine with id: {}", routineId);
                    return ResponseEntity.ok(updatedRoutine);
                })
                .orElseGet(() -> {
                    logger.warn("Routine not found for update: id: {}", routineId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{routineId}")
    public ResponseEntity<Void> deleteRoutine(@PathVariable Long routineId) {
        logger.info("Deleting routine with id: {}", routineId);
        if (routineService.deleteRoutine(routineId)) {
            logger.info("Successfully deleted routine with id: {}", routineId);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Routine not found for deletion: id: {}", routineId);
        return ResponseEntity.notFound().build();
    }
}
