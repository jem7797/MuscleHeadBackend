package com.MuscleHead.MuscleHead.LiveSession;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveSessionExerciseRepository extends JpaRepository<LiveSessionExercise, UUID> {

    @Query("SELECT e FROM LiveSessionExercise e WHERE e.session.id = :sessionId ORDER BY e.loggedAt ASC")
    List<LiveSessionExercise> findBySessionId(@Param("sessionId") UUID sessionId);
}
