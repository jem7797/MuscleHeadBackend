package com.MuscleHead.MuscleHead.WorkedMuscles;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkedMusclesRepository extends JpaRepository<WorkedMuscles, String> {

    Optional<WorkedMuscles> findByUserId(String userId);
}
