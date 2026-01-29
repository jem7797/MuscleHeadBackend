package com.MuscleHead.MuscleHead.Workout.SessionInstance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionInstanceRepository extends JpaRepository<SessionInstance, Long> {

    @Query("SELECT si FROM SessionInstance si WHERE si.user.sub_id = :subId")
    Page<SessionInstance> findByUser_SubId(@Param("subId") String subId, Pageable pageable);

    @Query("SELECT si FROM SessionInstance si WHERE si.user.sub_id = :subId")
    java.util.List<SessionInstance> findByUser_SubId(@Param("subId") String subId);

    @Query("SELECT si FROM SessionInstance si WHERE si.sessionLog.id = :sessionId")
    java.util.List<SessionInstance> findByWorkoutSessionId(@Param("sessionId") long sessionId);
}
