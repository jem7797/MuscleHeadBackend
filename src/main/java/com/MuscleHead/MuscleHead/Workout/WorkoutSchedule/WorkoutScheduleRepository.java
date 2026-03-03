package com.MuscleHead.MuscleHead.Workout.WorkoutSchedule;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutScheduleRepository extends JpaRepository<WorkoutSchedule, Long> {

    @Query("SELECT ws FROM WorkoutSchedule ws WHERE ws.user.sub_id = :userSubId")
    List<WorkoutSchedule> findByUserSubId(@Param("userSubId") String userSubId);
}
