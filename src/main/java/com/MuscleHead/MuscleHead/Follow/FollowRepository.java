package com.MuscleHead.MuscleHead.Follow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.MuscleHead.MuscleHead.User.User;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowee(User follower, User followee);

    void deleteByFollowerAndFollowee(User follower, User followee);

    List<Follow> findByFollowee(User followee);

    List<Follow> findByFollower(User follower);
}
