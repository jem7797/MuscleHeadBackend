package com.MuscleHead.MuscleHead.Medal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMedalRepository extends JpaRepository<UserMedal, Long> {

    @Query("SELECT um FROM UserMedal um WHERE um.user.sub_id = :subId ORDER BY um.awardedAt DESC")
    List<UserMedal> findByUserSubIdOrderByAwardedAtDesc(@Param("subId") String subId);

    @Query("SELECT COUNT(um) > 0 FROM UserMedal um WHERE um.user.sub_id = :subId AND um.medalName = :medalName")
    boolean existsByUserSubIdAndMedalName(@Param("subId") String subId, @Param("medalName") MedalName medalName);
}
