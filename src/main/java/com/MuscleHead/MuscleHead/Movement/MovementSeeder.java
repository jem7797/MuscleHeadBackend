package com.MuscleHead.MuscleHead.Movement;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MovementSeeder implements CommandLineRunner {

    private final MovementRepository movementRepository;

    public MovementSeeder(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Override
    public void run(String... args) {

    }

    public void seedMovements() {
        if (movementRepository.count() > 0) {
            return;
        }

        List<Movement> list = List.of(
                new Movement());

    }

}
