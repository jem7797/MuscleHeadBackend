package com.MuscleHead.MuscleHead.Post;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.user.sub_id IN :subIds ORDER BY p.timestamp DESC")
    Page<Post> findByUserSubIdIn(@Param("subIds") List<String> subIds, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.sub_id = :subId ORDER BY p.timestamp DESC")
    Page<Post> findByUserSubId(@Param("subId") String subId, Pageable pageable);
}
