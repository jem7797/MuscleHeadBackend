package com.MuscleHead.MuscleHead.Rank;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RankRepository extends JpaRepository<Rank, Long> {
    
    Optional<Rank> findByLevel(Integer level);
}
