package com.MuscleHead.MuscleHead.Post;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.Follow.FollowRepository;
import com.MuscleHead.MuscleHead.Post.Comment.Comment;
import com.MuscleHead.MuscleHead.Post.Like.Like;
import com.MuscleHead.MuscleHead.Post.Like.LikeId;
import com.MuscleHead.MuscleHead.Post.Like.LikeRepository;
import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.cache.RedisService;
import com.MuscleHead.MuscleHead.s3.S3Service;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private static final String POST_CACHE_PREFIX = "post:";
    private static final String FOLLOWING_CACHE_PREFIX = "following:";

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final RedisService redisService;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;
    private final int cacheTtlSeconds;
    private final int followingCacheTtlSeconds;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       FollowRepository followRepository,
                       LikeRepository likeRepository,
                       RedisService redisService,
                       S3Service s3Service,
                       ObjectMapper objectMapper,
                       @Value("${post.cache.ttl-seconds:3600}") int cacheTtlSeconds,
                       @Value("${following.cache.ttl-seconds:900}") int followingCacheTtlSeconds) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.likeRepository = likeRepository;
        this.redisService = redisService;
        this.s3Service = s3Service;
        this.objectMapper = objectMapper;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.followingCacheTtlSeconds = followingCacheTtlSeconds;
    }

    /**
     * Get post by ID: cache first, then DB. Returns 404 if not found in either.
     */
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID is required");
        }

        String cacheKey = POST_CACHE_PREFIX + postId;

        // 1. Check cache first
        String cached = redisService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            logger.debug("Post {} served from cache", postId);
            PostResponse resp = parsePostResponse(cached);
            enrichWithImageUrl(resp);
            return resp;
        }

        // 2. Hit DB
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        PostResponse response = PostResponse.from(post);

        // 3. Populate cache for next time
        try {
            String json = objectMapper.writeValueAsString(response);
            redisService.setWithTtl(cacheKey, json, cacheTtlSeconds);
            logger.debug("Post {} cached", postId);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to cache post {}: {}", postId, e.getMessage());
        }

        enrichWithImageUrl(response);
        return response;
    }

    /**
     * Create a post: save to DB and cache.
     */
    @Transactional
    public PostResponse createPost(User user, PostRequest request) {
        Post post = new Post();
        post.setUser(user);
        post.setImageLink(request.getImageLink());
        post.setCaption(request.getCaption() != null ? request.getCaption() : "");

        Post saved = postRepository.save(post);

        user.setNumber_of_posts(user.getNumber_of_posts() + 1);
        userRepository.save(user);

        PostResponse response = PostResponse.from(saved);
        cachePostResponse(response);
        enrichWithImageUrl(response);
        logger.info("Post {} created and cached", saved.getPostId());
        return response;
    }

    /**
     * Delete a post. Only the author can delete. Invalidates cache and decrements user post count.
     */
    @Transactional
    public void deletePost(Long postId, String requesterSubId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID is required");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        if (!post.getUser().getSub_id().equals(requesterSubId)) {
            throw new RuntimeException("Post not found: " + postId);
        }
        User author = post.getUser();
        author.setNumber_of_posts(Math.max(0, author.getNumber_of_posts() - 1));
        userRepository.save(author);
        postRepository.delete(post);
        redisService.delete(POST_CACHE_PREFIX + postId);
        logger.info("Post {} deleted", postId);
    }

    /**
     * Patch a post: increment like and/or add a comment.
     */
    @Transactional
    public PostResponse patchPost(Long postId, String requesterSubId, PostPatchRequest request) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID is required");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        User requester = userRepository.findById(requesterSubId)
                .orElseThrow(() -> new RuntimeException("User not found: " + requesterSubId));

        boolean changed = false;

        if (Boolean.TRUE.equals(request.getLike())) {
            if (!likeRepository.existsByPostIdAndUserSubId(postId, requesterSubId)) {
                Like like = new Like();
                like.setId(new LikeId(postId, requesterSubId));
                like.setPost(post);
                like.setUser(requester);
                likeRepository.save(like);
                post.setLikeCount(post.getLikeCount() + 1);
                changed = true;
            }
        }

        if (request.getComment() != null && !request.getComment().isBlank()) {
            Comment comment = new Comment();
            comment.setPost(post);
            comment.setUser(requester);
            comment.setText(request.getComment().trim());
            post.getComments().add(comment);
            post.setCommentCount(post.getCommentCount() + 1);
            changed = true;
        }

        if (!changed) {
            return PostResponse.from(post);
        }

        Post saved = postRepository.save(post);
        PostResponse response = PostResponse.from(saved);
        cachePostResponse(response);
        enrichWithImageUrl(response);
        logger.info("Post {} patched (like={}, comment={})", postId, request.getLike(), request.getComment() != null);
        return response;
    }

    /**
     * Get list of sub_ids that the user follows. Cache first, then DB.
     */
    @Transactional(readOnly = true)
    public List<String> getFollowedSubIds(String followerSubId) {
        if (followerSubId == null || followerSubId.isBlank()) {
            throw new IllegalArgumentException("Follower sub_id is required");
        }

        String cacheKey = FOLLOWING_CACHE_PREFIX + followerSubId;
        String cached = redisService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            logger.debug("Followed sub_ids for {} served from cache", followerSubId);
            return parseStringList(cached);
        }

        List<String> followeeSubIds = followRepository.findFolloweeSubIdsByFollowerSubId(followerSubId);
        try {
            String json = objectMapper.writeValueAsString(followeeSubIds);
            redisService.setWithTtl(cacheKey, json, followingCacheTtlSeconds);
            logger.debug("Followed sub_ids for {} cached ({} users)", followerSubId, followeeSubIds.size());
        } catch (JsonProcessingException e) {
            logger.warn("Failed to cache followed sub_ids for {}: {}", followerSubId, e.getMessage());
        }
        return followeeSubIds;
    }

    /**
     * Get feed: posts from users the current user follows, plus their own posts.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getFeedForUser(String followerSubId, Pageable pageable) {
        List<String> followedSubIds = getFollowedSubIds(followerSubId);
        if (!followedSubIds.contains(followerSubId)) {
            followedSubIds = new java.util.ArrayList<>(followedSubIds);
            followedSubIds.add(followerSubId);
        }
        Page<Post> posts = postRepository.findByUserSubIdIn(followedSubIds, pageable);
        Page<PostResponse> page = posts.map(PostResponse::from);
        page.forEach(this::enrichWithImageUrl);
        return page;
    }

    /**
     * Get posts by user sub_id (e.g. for profile page). Paginated.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUserId(String subId, Pageable pageable) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User sub_id is required");
        }
        Page<Post> posts = postRepository.findByUserSubId(subId, pageable);
        Page<PostResponse> page = posts.map(PostResponse::from);
        page.forEach(this::enrichWithImageUrl);
        return page;
    }

    /**
     * If imageLink is an S3 object key (not a full URL), replace it with a presigned download URL.
     */
    private void enrichWithImageUrl(PostResponse response) {
        if (response == null || response.getImageLink() == null || response.getImageLink().isBlank()) return;
        String link = response.getImageLink();
        if (link.startsWith("http://") || link.startsWith("https://")) return;
        try {
            String presignedUrl = s3Service.generatePresignedDownloadUrl(link);
            response.setImageLink(presignedUrl);
        } catch (Exception e) {
            logger.warn("Failed to generate presigned URL for image {}: {}", link, e.getMessage());
        }
    }

    private List<String> parseStringList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse cached following list: {}", e.getMessage());
            throw new RuntimeException("Cache data corrupted", e);
        }
    }

    private void cachePostResponse(PostResponse response) {
        if (response == null || response.getPostId() == null) return;
        try {
            String json = objectMapper.writeValueAsString(response);
            redisService.setWithTtl(POST_CACHE_PREFIX + response.getPostId(), json, cacheTtlSeconds);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to cache post {}: {}", response.getPostId(), e.getMessage());
        }
    }

    private PostResponse parsePostResponse(String json) {
        try {
            return objectMapper.readValue(json, PostResponse.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse cached post: {}", e.getMessage());
            throw new RuntimeException("Cache data corrupted", e);
        }
    }
}
