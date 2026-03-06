package com.MuscleHead.MuscleHead.Post;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT p FROM Post p LEFT JOIN FETCH p.achievement WHERE p.user.sub_id IN :subIds ORDER BY p.timestamp DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user.sub_id IN :subIds")
    Page<Post> findByUserSubIdIn(@Param("subIds") List<String> subIds, Pageable pageable);

    @Query(value = "SELECT p FROM Post p LEFT JOIN FETCH p.achievement WHERE p.user.sub_id = :subId ORDER BY p.timestamp DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.user.sub_id = :subId")
    Page<Post> findByUserSubId(@Param("subId") String subId, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.achievement WHERE p.postId = :postId")
    java.util.Optional<Post> findByIdWithAchievement(@Param("postId") Long postId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.sub_id = :subId")
    long countByUser_SubId(@Param("subId") String subId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.sub_id = :subId AND p.imageLink IS NOT NULL AND p.imageLink != ''")
    long countByUser_SubIdAndImageLinkNotNull(@Param("subId") String subId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.sub_id = :subId AND (p.imageLink IS NULL OR p.imageLink = '')")
    long countByUser_SubIdAndImageLinkNullOrEmpty(@Param("subId") String subId);

    @Query("SELECT COUNT(p) > 0 FROM Post p WHERE p.user.sub_id = :subId AND p.achievementId = :achievementId")
    boolean existsByUserSubIdAndAchievementId(@Param("subId") String subId, @Param("achievementId") Long achievementId);
}
