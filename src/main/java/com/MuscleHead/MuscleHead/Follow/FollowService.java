package com.MuscleHead.MuscleHead.Follow;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.Medal.MedalService;
import com.MuscleHead.MuscleHead.Notification.NotificationService;
import com.MuscleHead.MuscleHead.Post.PostService;
import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.cache.RedisService;

import jakarta.transaction.Transactional;

@Service
public class FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowService.class);
    private static final String FOLLOWING_CACHE_PREFIX = "following:";
    private static final String MUTUAL_CACHE_PREFIX = "mutual:";

    @Autowired
    private FollowRepository followRepository;

    @Value("${mutual-followers.cache.ttl-seconds:900}")
    private int mutualCacheTtlSeconds;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MedalService medalService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PostService postService;

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
        follow.setId(new FollowId(followerSubId, followeeSubId));
        follow.setFollower(follower);
        follow.setFollowee(followee);
        followRepository.save(follow);

        follower.setNumber_following(follower.getNumber_following() + 1);
        followee.setNumber_of_followers(followee.getNumber_of_followers() + 1);
        userRepository.save(follower);
        userRepository.save(followee);

        notificationService.createFollowNotification(followee, follower);
        medalService.checkFollowerMedals(followee);

        redisService.delete(FOLLOWING_CACHE_PREFIX + followerSubId);
        postService.invalidateFeedCacheForUser(followerSubId);
        invalidateMutualCacheForUser(followerSubId);
        invalidateMutualCacheForUser(followeeSubId);

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

        redisService.delete(FOLLOWING_CACHE_PREFIX + followerSubId);
        postService.invalidateFeedCacheForUser(followerSubId);
        invalidateMutualCacheForUser(followerSubId);
        invalidateMutualCacheForUser(followeeSubId);

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

    /**
     * Returns true if both users follow each other (mutual follow).
     * Cached for mutualCacheTtlSeconds; invalidated on follow/unfollow.
     */
    public boolean areMutualFollowers(String user1SubId, String user2SubId) {
        if (user1SubId == null || user2SubId == null || user1SubId.equals(user2SubId)) {
            return false;
        }
        String cacheKey = buildMutualCacheKey(user1SubId, user2SubId);
        String cached = redisService.get(cacheKey);
        if (cached != null) {
            return "true".equalsIgnoreCase(cached);
        }
        boolean result = isFollowing(user1SubId, user2SubId) && isFollowing(user2SubId, user1SubId);
        redisService.setWithTtl(cacheKey, String.valueOf(result), mutualCacheTtlSeconds);
        return result;
    }

    private String buildMutualCacheKey(String user1, String user2) {
        String a = user1.compareTo(user2) < 0 ? user1 : user2;
        String b = user1.compareTo(user2) < 0 ? user2 : user1;
        return MUTUAL_CACHE_PREFIX + a + ":" + b;
    }

    private void invalidateMutualCacheForUser(String userId) {
        redisService.deleteKeysByPattern(MUTUAL_CACHE_PREFIX + userId + ":*");
        redisService.deleteKeysByPattern(MUTUAL_CACHE_PREFIX + "*:" + userId);
    }
}
