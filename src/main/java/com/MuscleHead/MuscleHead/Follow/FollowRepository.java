package com.MuscleHead.MuscleHead.Follow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.MuscleHead.MuscleHead.User.User;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    boolean existsByFollowerAndFollowee(User follower, User followee);

    void deleteByFollowerAndFollowee(User follower, User followee);

    List<Follow> findByFollowee(User followee);

    List<Follow> findByFollower(User follower);

    @Query("SELECT f.id.followeeSubId FROM Follow f WHERE f.id.followerSubId = :followerSubId")
    List<String> findFolloweeSubIdsByFollowerSubId(@Param("followerSubId") String followerSubId);
}
