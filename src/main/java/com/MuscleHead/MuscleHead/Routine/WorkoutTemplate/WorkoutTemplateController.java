package com.MuscleHead.MuscleHead.Routine.WorkoutTemplate;

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

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.config.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("routine/api/")
@Validated
public class WorkoutTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutTemplateController.class);

    @Autowired
    private WorkoutTemplateService routineService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<WorkoutTemplateResponse> createRoutine(@Valid @RequestBody WorkoutTemplateRequest request) {
        logger.info("Creating new routine: {}", request.getName());

        // Resolve authenticated user
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            logger.error("Attempted to create routine without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(subId)
                .orElseThrow(() -> {
                    logger.error("User not found with sub_id: {}", subId);
                    return new RuntimeException("User not found: " + subId);
                });

        try {
            WorkoutTemplate createdRoutine = routineService.createRoutine(user, request);
            logger.info("Successfully created routine with id: {} for user: {}", createdRoutine.getId(), subId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new WorkoutTemplateResponse(createdRoutine.getId()));
        } catch (IllegalArgumentException ex) {
            logger.warn("Failed to create routine: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Error creating routine: {}", request.getName(), ex);
            throw ex;
        }
    }

    @GetMapping("/{routineId}")
    public ResponseEntity<WorkoutTemplate> getRoutineById(@PathVariable Long routineId) {
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
    public ResponseEntity<List<WorkoutTemplate>> getRoutines(@RequestParam(required = false) String subId) {
        if (subId != null && !subId.isBlank()) {
            logger.debug("Getting routines for user with sub_id: {}", subId);
            List<WorkoutTemplate> routines = routineService.getRoutinesByUserSubId(subId);
            return ResponseEntity.ok(routines);
        }
        logger.debug("Getting all routines");
        List<WorkoutTemplate> routines = routineService.getAllRoutines();
        return ResponseEntity.ok(routines);
    }

    @PutMapping("/{routineId}")
    public ResponseEntity<WorkoutTemplate> updateRoutine(
            @PathVariable Long routineId,
            @Valid @RequestBody WorkoutTemplate routine) {
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
