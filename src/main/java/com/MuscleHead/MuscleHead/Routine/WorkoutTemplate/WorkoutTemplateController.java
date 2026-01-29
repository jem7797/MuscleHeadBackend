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
@RequestMapping("workoutTemplate/api/")
@Validated
public class WorkoutTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutTemplateController.class);

    @Autowired
    private WorkoutTemplateService workoutTemplate;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<WorkoutTemplateResponse> createWorkoutTemplate(@Valid @RequestBody WorkoutTemplateRequest request) {
        logger.info("Creating new workout template: {}", request.getName());

        // Resolve authenticated user
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            logger.error("Attempted to create workout template without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(subId)
                .orElseThrow(() -> {
                    logger.error("User not found with sub_id: {}", subId);
                    return new RuntimeException("User not found: " + subId);
                });

        try {
            WorkoutTemplate createdWorkoutTemplate = workoutTemplate.createWorkoutTemplate(user, request);
            logger.info("Successfully created workout template with id: {} for user: {}", createdWorkoutTemplate.getId(), subId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new WorkoutTemplateResponse(createdWorkoutTemplate.getId()));
        } catch (IllegalArgumentException ex) {
            logger.warn("Failed to create workout template: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Error creating workout template: {}", request.getName(), ex);
            throw ex;
        }
    }

    @GetMapping("/{workoutTemplateId}")
    public ResponseEntity<WorkoutTemplate> getWorkoutTemplateById(@PathVariable Long workoutTemplateId) {
        logger.debug("Getting workout template by id: {}", workoutTemplateId);
        return workoutTemplate.getWorkoutTemplateById(workoutTemplateId)
                .map(workoutTemplate -> {
                    logger.debug("Found workout template with id: {}", workoutTemplateId);
                    return ResponseEntity.ok(workoutTemplate);
                })
                .orElseGet(() -> {
                    logger.warn("Workout template not found with id: {}", workoutTemplateId);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<List<WorkoutTemplate>> getWorkoutTemplate(@RequestParam(required = false) String subId) {
        if (subId != null && !subId.isBlank()) {
            logger.debug("Getting routines for user with sub_id: {}", subId);
            List<WorkoutTemplate> workoutTemplates = workoutTemplate.getWorkoutTemplateByUserSubId(subId);
            return ResponseEntity.ok(workoutTemplates);
        }
        logger.debug("Getting all routines");
        List<WorkoutTemplate> routines = workoutTemplate.getAllWorkoutTemplate();
        return ResponseEntity.ok(routines);
    }

    @PutMapping("/{workoutTemplateId}")
    public ResponseEntity<WorkoutTemplate> updateWorkoutTemplate(
            @PathVariable Long workoutTemplateId,
            @Valid @RequestBody WorkoutTemplate workoutTemplate) {
        logger.info("Updating workout template with id: {}", workoutTemplateId);
        workoutTemplate.setId(workoutTemplateId);
        return this.workoutTemplate.updateWorkoutTemplate(workoutTemplate)
                .map(updatedWorkoutTemplate -> {
                    logger.info("Successfully updated workout template with id: {}", workoutTemplateId);
                    return ResponseEntity.ok(updatedWorkoutTemplate);
                })
                .orElseGet(() -> {
                    logger.warn("Workout template not found for update: id: {}", workoutTemplateId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{workoutTemplateId}")
    public ResponseEntity<Void> deleteWorkoutTemplate(@PathVariable Long workoutTemplateId) {
        logger.info("Deleting workout template with id: {}", workoutTemplateId);
        if (workoutTemplate.deleteWorkoutTemplate(workoutTemplateId)) {
            logger.info("Successfully deleted workout template with id: {}", workoutTemplateId);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Workout template not found for deletion: id: {}", workoutTemplateId);
        return ResponseEntity.notFound().build();
    }
}
