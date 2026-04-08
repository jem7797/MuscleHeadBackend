package com.MuscleHead.MuscleHead.Movement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("movement/api/")
public class MovementController {

    @Autowired
    private MovementRepository movementRepository;

    /**
     * Returns all movements (id, name, areaOfActivation, description) so the frontend
     * can display exercises by name and use the id when creating session logs.
     */
    @GetMapping
    public ResponseEntity<List<Movement>> getAllMovements() {
        List<Movement> movements = movementRepository.findAll();
        return ResponseEntity.ok(movements);
    }
}
