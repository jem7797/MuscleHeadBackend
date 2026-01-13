package com.MuscleHead.MuscleHead.Routine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.MuscleHead.MuscleHead.User.User;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    List<Routine> findByUser(User user);

    List<Routine> findByUserSub_id(String subId);
}
