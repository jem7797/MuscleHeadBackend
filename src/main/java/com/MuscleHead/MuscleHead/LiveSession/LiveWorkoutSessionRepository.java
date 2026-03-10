package com.MuscleHead.MuscleHead.LiveSession;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveWorkoutSessionRepository extends JpaRepository<LiveWorkoutSession, UUID> {

    @Query("SELECT s FROM LiveWorkoutSession s WHERE (s.hostUserId = :userId OR s.guestUserId = :userId) AND s.status = 'in_progress'")
    Optional<LiveWorkoutSession> findActiveSessionForUser(@Param("userId") String userId);
}
