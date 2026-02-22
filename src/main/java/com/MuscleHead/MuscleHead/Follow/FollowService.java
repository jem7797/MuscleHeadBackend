package com.MuscleHead.MuscleHead.Follow;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowService.class);

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void follow(String followerSubId, String followeeSubId) {
        if (followerSubId == null || followeeSubId == null) {
            throw new IllegalArgumentException("Follower and followee sub_ids must not be null");
        }
        if (followerSubId.equals(followeeSubId)) {
            throw new IllegalArgumentException("Users cannot follow themselves");
        }

        User follower = userRepository.findById(followerSubId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found: " + followerSubId));
        User followee = userRepository.findById(followeeSubId)
                .orElseThrow(() -> new IllegalArgumentException("Followee not found: " + followeeSubId));

        if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new IllegalStateException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowee(followee);
        followRepository.save(follow);

        follower.setNumber_following(follower.getNumber_following() + 1);
        followee.setNumber_of_followers(followee.getNumber_of_followers() + 1);
        userRepository.save(follower);
        userRepository.save(followee);

        logger.info("User {} followed user {}", followerSubId, followeeSubId);
    }

    @Transactional
    public void unfollow(String followerSubId, String followeeSubId) {
        if (followerSubId == null || followeeSubId == null) {
            throw new IllegalArgumentException("Follower and followee sub_ids must not be null");
        }

        User follower = userRepository.findById(followerSubId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found: " + followerSubId));
        User followee = userRepository.findById(followeeSubId)
                .orElseThrow(() -> new IllegalArgumentException("Followee not found: " + followeeSubId));

        if (!followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new IllegalStateException("Not following this user");
        }

        followRepository.deleteByFollowerAndFollowee(follower, followee);

        follower.setNumber_following(Math.max(0, follower.getNumber_following() - 1));
        followee.setNumber_of_followers(Math.max(0, followee.getNumber_of_followers() - 1));
        userRepository.save(follower);
        userRepository.save(followee);

        logger.info("User {} unfollowed user {}", followerSubId, followeeSubId);
    }

    public List<UserSummary> getFollowers(String subId) {
        User user = userRepository.findById(subId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + subId));
        return followRepository.findByFollowee(user).stream()
                .map(f -> UserSummary.from(f.getFollower()))
                .collect(Collectors.toList());
    }

    public List<UserSummary> getFollowing(String subId) {
        User user = userRepository.findById(subId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + subId));
        return followRepository.findByFollower(user).stream()
                .map(f -> UserSummary.from(f.getFollowee()))
                .collect(Collectors.toList());
    }

    public boolean isFollowing(String followerSubId, String followeeSubId) {
        if (followerSubId == null || followeeSubId == null) return false;
        return userRepository.findById(followerSubId)
                .flatMap(follower -> userRepository.findById(followeeSubId)
                        .map(followee -> followRepository.existsByFollowerAndFollowee(follower, followee)))
                .orElse(false);
    }
}
