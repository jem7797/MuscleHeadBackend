package com.MuscleHead.MuscleHead.Workout;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @Query("SELECT w FROM Workout w WHERE w.user.sub_id = :subId")
    Page<Workout> findByUser_SubId(@Param("subId") String subId, Pageable pageable);

    @Query("SELECT w FROM Workout w WHERE w.user.sub_id = :subId")
    java.util.List<Workout> findByUser_SubId(@Param("subId") String subId);
}
