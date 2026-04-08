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
    public ResponseEntity<WorkoutTemplateResponse> createWorkoutTemplate(
            @Valid @RequestBody WorkoutTemplateRequest request) {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(subId)
                .orElseThrow(() -> new RuntimeException("User not found: " + subId));

        try {
            WorkoutTemplate createdWorkoutTemplate = workoutTemplate.createWorkoutTemplate(user, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new WorkoutTemplateResponse(createdWorkoutTemplate.getId()));
        } catch (IllegalArgumentException ex) {
            logger.warn("Failed to create workout template: {}", ex.getMessage());
            throw ex;
        }
    }

    @GetMapping("/{workoutTemplateId}")
    public ResponseEntity<WorkoutTemplate> getWorkoutTemplateById(@PathVariable Long workoutTemplateId) {
        return workoutTemplate.getWorkoutTemplateById(workoutTemplateId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Workout template not found with id: {}", workoutTemplateId);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<List<WorkoutTemplate>> getWorkoutTemplate(@RequestParam(required = false) String subId) {
        if (subId != null && !subId.isBlank()) {
            List<WorkoutTemplate> workoutTemplates = workoutTemplate.getWorkoutTemplateByUserSubId(subId);
            return ResponseEntity.ok(workoutTemplates);
        }
        List<WorkoutTemplate> routines = workoutTemplate.getAllWorkoutTemplate();
        return ResponseEntity.ok(routines);
    }

    @PutMapping("/{workoutTemplateId}")
    public ResponseEntity<WorkoutTemplate> updateWorkoutTemplate(
            @PathVariable Long workoutTemplateId,
            @Valid @RequestBody WorkoutTemplate workoutTemplate) {
        workoutTemplate.setId(workoutTemplateId);
        return this.workoutTemplate.updateWorkoutTemplate(workoutTemplate)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Workout template not found for update: id: {}", workoutTemplateId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/all")
    public ResponseEntity<Integer> deleteAllWorkoutTemplates() {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        int deleted = workoutTemplate.deleteAllWorkoutTemplates();
        return ResponseEntity.ok(deleted);
    }

    @DeleteMapping("/{workoutTemplateId}")
    public ResponseEntity<Void> deleteWorkoutTemplate(@PathVariable Long workoutTemplateId) {
        if (workoutTemplate.deleteWorkoutTemplate(workoutTemplateId)) {
            return ResponseEntity.noContent().build();
        }
        logger.warn("Workout template not found for deletion: id: {}", workoutTemplateId);
        return ResponseEntity.notFound().build();
    }
}
