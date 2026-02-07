package com.MuscleHead.MuscleHead.Movement;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("movement/api/")
public class MovementController {

    private static final Logger logger = LoggerFactory.getLogger(MovementController.class);

    @Autowired
    private MovementRepository movementRepository;

    /**
     * Returns all movements (id, name, areaOfActivation, description) so the frontend
     * can display exercises by name and use the id when creating session logs.
     */
    @GetMapping
    public ResponseEntity<List<Movement>> getAllMovements() {
        logger.debug("Fetching all movements");
        List<Movement> movements = movementRepository.findAll();
        logger.debug("Found {} movements", movements.size());
        return ResponseEntity.ok(movements);
    }
}
