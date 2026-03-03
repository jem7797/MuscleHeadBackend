package com.MuscleHead.MuscleHead.Workout.SessionLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {

    @Query(value = "SELECT sl FROM SessionLog sl WHERE sl.user.sub_id = :subId",
            countQuery = "SELECT COUNT(sl) FROM SessionLog sl WHERE sl.user.sub_id = :subId")
    Page<SessionLog> findByUser_SubId(@Param("subId") String subId, Pageable pageable);

    @Query("SELECT sl FROM SessionLog sl WHERE sl.user.sub_id = :subId")
    java.util.List<SessionLog> findByUser_SubId(@Param("subId") String subId);

    @Modifying
    @Query("UPDATE SessionLog s SET s.routine = null WHERE s.routine IS NOT NULL")
    void clearAllRoutineReferences();
}
