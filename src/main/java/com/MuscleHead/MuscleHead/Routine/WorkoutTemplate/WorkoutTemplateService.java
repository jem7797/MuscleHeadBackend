package com.MuscleHead.MuscleHead.Routine.WorkoutTemplate;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.Movement.Movement;
import com.MuscleHead.MuscleHead.Movement.MovementRepository;
import com.MuscleHead.MuscleHead.Routine.ExerciseInstance.ExerciseInstance;
import com.MuscleHead.MuscleHead.Routine.ExerciseInstance.ExerciseInstanceRepository;
import com.MuscleHead.MuscleHead.Routine.ExerciseInstance.ExerciseInstanceRequest;
import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class WorkoutTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutTemplateService.class);

    @Autowired
    private WorkoutTemplateRepository workoutTemplateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovementRepository exerciseRepository;

    @Autowired
    private ExerciseInstanceRepository exerciseInstanceRepository;

    @Transactional
    public WorkoutTemplate createWorkoutTemplate(User user, WorkoutTemplateRequest request) {
        logger.debug("Creating new workout template: {} for user: {}",
                request != null ? request.getName() : "null", user != null ? user.getSub_id() : "null");

        if (user == null || user.getSub_id() == null) {
            logger.error("Attempted to create workout template with null user or sub_id");
            throw new IllegalArgumentException("User is required");
        }

        if (request == null || request.getName() == null || request.getName().isBlank()) {
            logger.error("Attempted to create workout template with null or blank name");
            throw new IllegalArgumentException("Workout template name is required");
        }

        if (request.getExercises() == null || request.getExercises().isEmpty()) {
            logger.error("Attempted to create workout template with null or empty exercises list");
            throw new IllegalArgumentException("Exercises list is required and cannot be empty");
        }

        // Ensure user exists
        User existingUser = userRepository.findById(user.getSub_id())
                .orElseThrow(() -> {
                    logger.error("User not found with sub_id: {}", user.getSub_id());
                    return new IllegalArgumentException("User not found with sub_id: " + user.getSub_id());
                });

        // Create WorkoutTemplate
        WorkoutTemplate workoutTemplate = new WorkoutTemplate();
        workoutTemplate.setUser(existingUser);
        workoutTemplate.setName(request.getName());

        // Create ExerciseInstance entities
        java.util.List<ExerciseInstance> exerciseInstances = new java.util.ArrayList<>();
        for (ExerciseInstanceRequest exerciseRequest : request.getExercises()) {
            // Find Exercise
            Movement exercise = exerciseRepository.findById(exerciseRequest.getExerciseId())
                    .orElseThrow(() -> {
                        logger.warn("Exercise not found with id: {}", exerciseRequest.getExerciseId());
                        return new IllegalArgumentException("Exercise not found: " + exerciseRequest.getExerciseId());
                    });

            // Create ExerciseInstance
            ExerciseInstance exerciseInstance = new ExerciseInstance();
            exerciseInstance.setRoutine(workoutTemplate);
            exerciseInstance.setExercise(exercise);
            exerciseInstance.setOrderIndex(exerciseRequest.getOrderIndex());
            exerciseInstance.setReps(exerciseRequest.getReps());
            exerciseInstance.setSets(exerciseRequest.getSets());

            exerciseInstances.add(exerciseInstance);
        }

        workoutTemplate.setRoutineExercises(exerciseInstances);

        // Save workout template (cascade will save exercises)
        WorkoutTemplate savedWorkoutTemplate = workoutTemplateRepository.save(workoutTemplate);
        logger.info("Workout template created successfully with id: {} for user: {} with {} exercises",
                savedWorkoutTemplate.getId(), existingUser.getSub_id(), exerciseInstances.size());

        return savedWorkoutTemplate;
    }

    @Transactional
    public WorkoutTemplate createNewWorkoutTemplate(WorkoutTemplate workoutTemplate) {
        logger.debug("Creating new workout template: {}", workoutTemplate != null ? workoutTemplate.getName() : "null");
        if (workoutTemplate == null || workoutTemplate.getUser() == null) {
            logger.error("Attempted to create workout template with null workout template or user");
            throw new IllegalArgumentException("Workout template and user must exist and not be null");
        }

        // Ensure user exists
        User user = workoutTemplate.getUser();
        if (user.getSub_id() == null) {
            logger.error("Attempted to create workout template with user missing sub_id");
            throw new IllegalArgumentException("User sub_id must not be null");
        }
        User existingUser = userRepository.findById(user.getSub_id())
                .orElseThrow(() -> {
                    logger.error("User not found with sub_id: {}", user.getSub_id());
                    return new IllegalArgumentException("User not found with sub_id: " + user.getSub_id());
                });
        workoutTemplate.setUser(existingUser);

        // Set workout template reference on all ExerciseInstances and ensure exercises exist
        if (workoutTemplate.getRoutineExercises() != null) {
            for (ExerciseInstance exerciseInstance : workoutTemplate.getRoutineExercises()) {
                exerciseInstance.setRoutine(workoutTemplate);
                if (exerciseInstance.getExercise() != null && exerciseInstance.getExercise().getId() != null) {
                    // Exercise already exists, fetch it
                    Movement exercise = exerciseRepository.findById(exerciseInstance.getExercise().getId())
                            .orElseThrow(() -> {
                                logger.error("Exercise not found with id: {}", exerciseInstance.getExercise().getId());
                                return new IllegalArgumentException(
                                        "Exercise not found with id: " + exerciseInstance.getExercise().getId());
                            });
                    exerciseInstance.setExercise(exercise);
                } else if (exerciseInstance.getExercise() != null && exerciseInstance.getExercise().getName() != null) {
                    // Try to find by name, or create new exercise
                    Movement exercise = exerciseRepository.findByName(exerciseInstance.getExercise().getName())
                            .orElseGet(() -> {
                                logger.debug("Creating new exercise: {}", exerciseInstance.getExercise().getName());
                                return exerciseRepository.save(exerciseInstance.getExercise());
                            });
                    exerciseInstance.setExercise(exercise);
                }
            }
        }

        WorkoutTemplate savedWorkoutTemplate = workoutTemplateRepository.save(workoutTemplate);
        logger.info("Workout template created successfully with id: {}", savedWorkoutTemplate.getId());
        return savedWorkoutTemplate;
    }

    @Transactional
    public Optional<WorkoutTemplate> updateWorkoutTemplate(WorkoutTemplate updatedWorkoutTemplate) {
        logger.debug("Updating workout template with id: {}", updatedWorkoutTemplate != null ? updatedWorkoutTemplate.getId() : "null");
        if (updatedWorkoutTemplate == null || updatedWorkoutTemplate.getId() == null) {
            logger.error("Attempted to update workout template with null workout template or id");
            throw new IllegalArgumentException("Workout template and id must not be null");
        }

        return workoutTemplateRepository.findById(updatedWorkoutTemplate.getId())
                .map(existingWorkoutTemplate -> {
                    logger.debug("Found existing workout template, updating fields for id: {}", updatedWorkoutTemplate.getId());
                    if (updatedWorkoutTemplate.getName() != null) {
                        existingWorkoutTemplate.setName(updatedWorkoutTemplate.getName());
                    }

                    // Handle ExerciseInstances update
                    if (updatedWorkoutTemplate.getRoutineExercises() != null) {
                        // Remove existing ExerciseInstances (orphanRemoval will handle deletion)
                        existingWorkoutTemplate.getRoutineExercises().clear();

                        // Add new ExerciseInstances
                        for (ExerciseInstance exerciseInstance : updatedWorkoutTemplate.getRoutineExercises()) {
                            exerciseInstance.setRoutine(existingWorkoutTemplate);
                            if (exerciseInstance.getExercise() != null
                                    && exerciseInstance.getExercise().getId() != null) {
                                // Exercise already exists, fetch it
                                Movement exercise = exerciseRepository.findById(exerciseInstance.getExercise().getId())
                                        .orElseThrow(() -> {
                                            logger.error("Exercise not found with id: {}",
                                                    exerciseInstance.getExercise().getId());
                                            return new IllegalArgumentException("Exercise not found with id: "
                                                    + exerciseInstance.getExercise().getId());
                                        });
                                exerciseInstance.setExercise(exercise);
                            } else if (exerciseInstance.getExercise() != null
                                    && exerciseInstance.getExercise().getName() != null) {
                                // Try to find by name, or create new exercise
                                Movement exercise = exerciseRepository
                                        .findByName(exerciseInstance.getExercise().getName())
                                        .orElseGet(() -> {
                                            logger.debug("Creating new exercise: {}",
                                                    exerciseInstance.getExercise().getName());
                                            return exerciseRepository.save(exerciseInstance.getExercise());
                                        });
                                exerciseInstance.setExercise(exercise);
                            }
                            existingWorkoutTemplate.getRoutineExercises().add(exerciseInstance);
                        }
                    }

                    WorkoutTemplate savedWorkoutTemplate = workoutTemplateRepository.save(existingWorkoutTemplate);
                    logger.info("Workout template updated successfully with id: {}", savedWorkoutTemplate.getId());
                    return savedWorkoutTemplate;
                });
    }

    @Transactional
    public boolean deleteWorkoutTemplate(Long workoutTemplateId) {
        logger.debug("Deleting workout template with id: {}", workoutTemplateId);
        if (workoutTemplateId == null) {
            logger.warn("Attempted to delete workout template with null id");
            return false;
        }
        if (!workoutTemplateRepository.existsById(workoutTemplateId)) {
            logger.warn("Workout template not found for deletion: id: {}", workoutTemplateId);
            return false;
        }
        workoutTemplateRepository.deleteById(workoutTemplateId);
        logger.info("Workout template deleted successfully with id: {}", workoutTemplateId);
        return true;
    }

    public Optional<WorkoutTemplate> getWorkoutTemplateById(Long workoutTemplateId) {
        logger.debug("Getting workout template by id: {}", workoutTemplateId);
        if (workoutTemplateId == null) {
            logger.error("Attempted to get workout template with null id");
            throw new IllegalArgumentException("Workout template id must not be null");
        }
        Optional<WorkoutTemplate> workoutTemplate = workoutTemplateRepository.findById(workoutTemplateId);
        if (workoutTemplate.isPresent()) {
            logger.debug("Found workout template with id: {}", workoutTemplateId);
        } else {
            logger.debug("Workout template not found with id: {}", workoutTemplateId);
        }
        return workoutTemplate;
    }

    public List<WorkoutTemplate> getWorkoutTemplateByUserSubId(String subId) {
        logger.debug("Getting workout templates for user with sub_id: {}", subId);
        if (subId == null || subId.isBlank()) {
            logger.error("Attempted to get workout templates with null or blank sub_id");
            throw new IllegalArgumentException("User sub_id must not be blank");
        }
        List<WorkoutTemplate> workoutTemplates = workoutTemplateRepository.findByUserSub_id(subId);
        logger.debug("Found {} workout templates for user with sub_id: {}", workoutTemplates.size(), subId);
        return workoutTemplates;
    }

    public List<WorkoutTemplate> getAllWorkoutTemplate() {
        logger.debug("Getting all workout templates");
        return workoutTemplateRepository.findAll();
    }
}