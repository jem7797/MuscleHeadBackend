package com.MuscleHead.MuscleHead.Routine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, Long> {

    List<RoutineExercise> findByRoutineId(Long routineId);

    void deleteByRoutineId(Long routineId);
}
