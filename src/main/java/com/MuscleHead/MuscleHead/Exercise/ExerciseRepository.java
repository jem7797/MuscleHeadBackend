package com.MuscleHead.MuscleHead.Exercise;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    Optional<Exercise> findByName(String name);
}
