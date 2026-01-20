package com.MuscleHead.MuscleHead.Workout;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user.sub_id = :subId")
    Page<WorkoutSession> findByUser_SubId(@Param("subId") String subId, Pageable pageable);

    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user.sub_id = :subId")
    java.util.List<WorkoutSession> findByUser_SubId(@Param("subId") String subId);
}
