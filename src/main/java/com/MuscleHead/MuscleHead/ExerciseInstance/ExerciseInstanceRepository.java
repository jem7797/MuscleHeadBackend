package com.MuscleHead.MuscleHead.ExerciseInstance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseInstanceRepository extends JpaRepository<ExerciseInstance, Long> {

    List<ExerciseInstance> findByRoutineId(Long routineId);

    void deleteByRoutineId(Long routineId);
}
