package com.MuscleHead.MuscleHead.Routine.WorkoutTemplate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.MuscleHead.MuscleHead.User.User;

public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {

    List<WorkoutTemplate> findByUser(User user);

    @Query("SELECT r FROM Routine r WHERE r.user.sub_id = :subId")
    List<WorkoutTemplate> findByUserSub_id(@Param("subId") String subId);
}
