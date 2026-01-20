package com.MuscleHead.MuscleHead.Routine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.MuscleHead.MuscleHead.User.User;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    List<Routine> findByUser(User user);

    @Query("SELECT r FROM Routine r WHERE r.user.sub_id = :subId")
    List<Routine> findByUserSub_id(@Param("subId") String subId);
}
