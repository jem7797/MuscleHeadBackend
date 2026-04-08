package com.MuscleHead.MuscleHead.Follow;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final String FOLLOWERS_LIST_PREFIX = "follow:followers:";
    private static final String FOLLOWING_LIST_PREFIX = "follow:following:";
    private static final String IS_FOLLOWING_PREFIX = "follow:check:";

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private FollowRequestRepository followRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mutual-followers.cache.ttl-seconds:900}")
    private int mutualCacheTtlSeconds;

    @Value("${follow-lists.cache.ttl-seconds:900}")
    private int followListsCacheTtlSeconds;

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

        if (isPrivate(followee)) {
            createFollowRequest(followerSubId, followeeSubId);
            return;
        }

        createFollowAndNotify(follower, followee, followerSubId, followeeSubId);
    }

    private boolean isPrivate(User user) {
        return user != null && "private".equalsIgnoreCase(user.getPrivacy_setting());
    }

    @Transactional
    public void createFollowRequest(String requesterSubId, String followeeSubId) {
        if (requesterSubId == null || followeeSubId == null) {
            throw new IllegalArgumentException("Requester and followee sub_ids must not be null");
        }
        if (requesterSubId.equals(followeeSubId)) {
            throw new IllegalArgumentException("Users cannot request to follow themselves");
        }

        User requester = userRepository.findById(requesterSubId)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found: " + requesterSubId));
        User followee = userRepository.findById(followeeSubId)
                .orElseThrow(() -> new IllegalArgumentException("Followee not found: " + followeeSubId));

        if (followRepository.existsByFollowerAndFollowee(requester, followee)) {
            throw new IllegalStateException("Already following this user");
        }

        if (followRequestRepository.findPendingByRequesterAndFollowee(requesterSubId, followeeSubId).isPresent()) {
            throw new IllegalStateException("Follow request already pending");
        }

        FollowRequest request = new FollowRequest();
        request.setRequester(requester);
        request.setFollowee(followee);
        request.setStatus(FollowRequest.RequestStatus.pending);
        followRequestRepository.save(request);
    }

    public List<FollowRequestResponse> getPendingRequests(String followeeSubId) {
        User followee = userRepository.findById(followeeSubId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + followeeSubId));
        return followRequestRepository.findPendingByFollowee(followee).stream()
                .map(this::toFollowRequestResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptRequest(java.util.UUID requestId, String followeeSubId) {
        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Follow request not found: " + requestId));
        if (!request.getFollowee().getSub_id().equals(followeeSubId)) {
            throw new IllegalArgumentException("Request does not belong to user");
        }
        if (request.getStatus() != FollowRequest.RequestStatus.pending) {
            throw new IllegalStateException("Request is no longer pending");
        }
        request.setStatus(FollowRequest.RequestStatus.accepted);
        followRequestRepository.save(request);
        String requesterSubId = request.getRequester().getSub_id();
        createFollowAndNotify(request.getRequester(), request.getFollowee(), requesterSubId, followeeSubId);
        invalidateFollowListsCache(requesterSubId, followeeSubId);
        invalidateIsFollowingCache(requesterSubId, followeeSubId);
    }

    @Transactional
    public void declineRequest(java.util.UUID requestId, String followeeSubId) {
        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Follow request not found: " + requestId));
        if (!request.getFollowee().getSub_id().equals(followeeSubId)) {
            throw new IllegalArgumentException("Request does not belong to user");
        }
        if (request.getStatus() != FollowRequest.RequestStatus.pending) {
            throw new IllegalStateException("Request is no longer pending");
        }
        request.setStatus(FollowRequest.RequestStatus.declined);
        followRequestRepository.save(request);
    }

    public String getRequestStatus(String requesterSubId, String followeeSubId) {
        if (requesterSubId == null || followeeSubId == null) return "none";
        return followRequestRepository.findPendingByRequesterAndFollowee(requesterSubId, followeeSubId)
                .isPresent() ? "pending" : "none";
    }

    private FollowRequestResponse toFollowRequestResponse(FollowRequest request) {
        return new FollowRequestResponse(
                request.getId(),
                UserSummary.from(request.getRequester()),
                request.getFollowee().getSub_id(),
                request.getStatus().name(),
                request.getCreatedAt());
    }

    private void createFollowAndNotify(User follower, User followee, String followerSubId, String followeeSubId) {
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
        invalidateFollowListsCache(followerSubId, followeeSubId);
        invalidateIsFollowingCache(followerSubId, followeeSubId);
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
        invalidateFollowListsCache(followerSubId, followeeSubId);
        invalidateIsFollowingCache(followerSubId, followeeSubId);
    }

    public List<UserSummary> getFollowers(String subId) {
        String cacheKey = FOLLOWERS_LIST_PREFIX + subId;
        String cached = redisService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<UserSummary>>() {});
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse cached followers for {}: {}", subId, e.getMessage());
            }
        }

        User user = userRepository.findById(subId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + subId));
        List<UserSummary> result = followRepository.findByFolloweeExcludingHiddenFollowers(user).stream()
                .map(f -> UserSummary.from(f.getFollower()))
                .collect(Collectors.toList());
        try {
            redisService.setWithTtl(cacheKey, objectMapper.writeValueAsString(result), followListsCacheTtlSeconds);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to cache followers for {}: {}", subId, e.getMessage());
        }
        return result;
    }

    public List<UserSummary> getFollowing(String subId) {
        String cacheKey = FOLLOWING_LIST_PREFIX + subId;
        String cached = redisService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<UserSummary>>() {});
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse cached following for {}: {}", subId, e.getMessage());
            }
        }

        User user = userRepository.findById(subId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + subId));
        List<UserSummary> result = followRepository.findByFollowerExcludingHiddenFollowees(user).stream()
                .map(f -> UserSummary.from(f.getFollowee()))
                .collect(Collectors.toList());
        try {
            redisService.setWithTtl(cacheKey, objectMapper.writeValueAsString(result), followListsCacheTtlSeconds);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to cache following for {}: {}", subId, e.getMessage());
        }
        return result;
    }

    public boolean isFollowing(String followerSubId, String followeeSubId) {
        if (followerSubId == null || followeeSubId == null) return false;

        String cacheKey = IS_FOLLOWING_PREFIX + followerSubId + ":" + followeeSubId;
        String cached = redisService.get(cacheKey);
        if (cached != null) {
            return "true".equalsIgnoreCase(cached);
        }

        boolean result = userRepository.findById(followerSubId)
                .flatMap(follower -> userRepository.findById(followeeSubId)
                        .map(followee -> followRepository.existsByFollowerAndFollowee(follower, followee)))
                .orElse(false);
        redisService.setWithTtl(cacheKey, String.valueOf(result), followListsCacheTtlSeconds);
        return result;
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

    private void invalidateFollowListsCache(String followerSubId, String followeeSubId) {
        redisService.delete(FOLLOWERS_LIST_PREFIX + followeeSubId);
        redisService.delete(FOLLOWING_LIST_PREFIX + followerSubId);
    }

    private void invalidateIsFollowingCache(String followerSubId, String followeeSubId) {
        redisService.delete(IS_FOLLOWING_PREFIX + followerSubId + ":" + followeeSubId);
    }
}
