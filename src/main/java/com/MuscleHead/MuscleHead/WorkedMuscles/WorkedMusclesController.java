package com.MuscleHead.MuscleHead.WorkedMuscles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MuscleHead.MuscleHead.config.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping({ "workedMuscles/api/", "api/worked-muscles" })
public class WorkedMusclesController {

    private static final Logger logger = LoggerFactory.getLogger(WorkedMusclesController.class);

    @Autowired
    private WorkedMusclesService workedMusclesService;

    @PostMapping
    public ResponseEntity<Void> upsertWorkedMuscles(@Valid @RequestBody WorkedMusclesPostRequest request) {
        String authSubId = SecurityUtils.getCurrentUserSub();
        if (authSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!authSubId.equals(request.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        workedMusclesService.upsertFromExercises(request.getUserId(), request.getExercises());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WorkedMusclesResponse> getWorkedMuscles(@PathVariable("userId") String userId) {
        String authSubId = SecurityUtils.getCurrentUserSub();
        if (authSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WorkedMusclesResponse response = workedMusclesService.getWorkedMuscles(userId);
        return ResponseEntity.ok(response);
    }
}
