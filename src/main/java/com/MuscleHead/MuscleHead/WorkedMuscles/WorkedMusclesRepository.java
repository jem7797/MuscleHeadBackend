package com.MuscleHead.MuscleHead.WorkedMuscles;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkedMusclesRepository extends JpaRepository<WorkedMuscles, Long> {

    List<WorkedMuscles> findByUserIdAndExpiresAtAfter(String userId, LocalDateTime now);

    @Modifying
    @Query(value = """
        INSERT INTO worked_muscles (user_id, muscle_group, expires_at)
        VALUES (:userId, :muscleGroup, NOW() + make_interval(hours => :hours))
        ON CONFLICT (user_id, muscle_group)
        DO UPDATE SET expires_at = NOW() + make_interval(hours => :hours)
        """, nativeQuery = true)
    void upsertMuscleGroup(@Param("userId") String userId, @Param("muscleGroup") String muscleGroup, @Param("hours") int hours);

    @Modifying
    @Query(value = "DELETE FROM worked_muscles WHERE expires_at < NOW()", nativeQuery = true)
    int deleteExpiredRows();
}
