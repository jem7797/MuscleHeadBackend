package com.MuscleHead.MuscleHead.Movement;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementRepository extends JpaRepository<Movement, Long> {

    Optional<Movement> findByName(String name);
}
