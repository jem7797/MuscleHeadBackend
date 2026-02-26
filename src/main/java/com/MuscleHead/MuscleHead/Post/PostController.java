package com.MuscleHead.MuscleHead.Post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.config.SecurityUtils;
import com.MuscleHead.MuscleHead.s3.S3Service;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("posts/api")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    private static final String POST_IMAGE_PREFIX = "posts/";

    private final PostService postService;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public PostController(PostService postService, UserRepository userRepository, S3Service s3Service) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }

    /**
     * Get presigned upload URL for a post image. Client uploads to S3, then uses objectKey
     * as imageLink when creating the post.
     */
    @PostMapping("/presigned-image-url")
    public ResponseEntity<PostImageUploadResponse> getPresignedImageUrl() {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String objectKey = POST_IMAGE_PREFIX + subId + "/" + UUID.randomUUID() + ".jpg";
        String uploadUrl = s3Service.generatePresignedUploadUrl(objectKey);
        return ResponseEntity.ok(new PostImageUploadResponse(uploadUrl, objectKey));
    }

    @GetMapping("/user/{subId}")
    public ResponseEntity<Page<PostResponse>> getPostsByUser(
            @PathVariable("subId") String subId,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponse> posts = postService.getPostsByUserId(subId, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Page<PostResponse> feed = postService.getFeedForUser(subId, pageable);
        return ResponseEntity.ok(feed);
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findById(subId)
                .orElseThrow(() -> new RuntimeException("User not found: " + subId));
        PostResponse created = postService.createPost(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable("id") Long postId) {
        logger.debug("Getting post by id: {}", postId);
        PostResponse post = postService.getPostById(postId);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long postId) {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        postService.deletePost(postId, subId);
        return ResponseEntity.noContent().build();
    }
}
