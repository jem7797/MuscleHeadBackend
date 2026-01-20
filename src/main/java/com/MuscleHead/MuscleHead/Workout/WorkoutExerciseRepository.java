package com.MuscleHead.MuscleHead.Workout;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    @Query("SELECT we FROM WorkoutExercise we WHERE we.user.sub_id = :subId")
    Page<WorkoutExercise> findByUser_SubId(@Param("subId") String subId, Pageable pageable);

    @Query("SELECT we FROM WorkoutExercise we WHERE we.user.sub_id = :subId")
    java.util.List<WorkoutExercise> findByUser_SubId(@Param("subId") String subId);

    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.id = :sessionId")
    java.util.List<WorkoutExercise> findByWorkoutSessionId(@Param("sessionId") long sessionId);
}
