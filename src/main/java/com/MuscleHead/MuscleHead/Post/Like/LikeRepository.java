package com.MuscleHead.MuscleHead.Post.Like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    @Query("SELECT COUNT(l) > 0 FROM Like l WHERE l.post.postId = :postId AND l.user.sub_id = :userSubId")
    boolean existsByPostIdAndUserSubId(@Param("postId") Long postId, @Param("userSubId") String userSubId);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.post.postId = :postId AND l.user.sub_id = :userSubId")
    void deleteByPostIdAndUserSubId(@Param("postId") Long postId, @Param("userSubId") String userSubId);
}
