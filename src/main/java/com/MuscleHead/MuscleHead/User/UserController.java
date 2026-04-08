package com.MuscleHead.MuscleHead.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.validation.annotation.Validated;

import com.MuscleHead.MuscleHead.config.SecurityUtils;
import com.MuscleHead.MuscleHead.s3.S3Service;
import com.MuscleHead.MuscleHead.validation.OnCreate;
import com.MuscleHead.MuscleHead.validation.OnUpdate;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;

@RestController
@RequestMapping("user/api/")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private S3Service s3Service;

    @PostMapping("minor-signup-attempt")
    public ResponseEntity<Void> recordMinorSignupAttempt(
            @Valid @RequestBody MinorSignupAttemptRequest request) {
        userService.recordMinorSignupAttempt(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<User> createUser(
        @Validated({OnCreate.class, Default.class}) 
        @RequestBody User user
    ) {
        try {
            User createdUser = userService.createNewUser(user);
            enrichProfilePicUrl(createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalStateException ex) {
            logger.warn("Failed to create user - user already exists: {}", user.getSub_id());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PatchMapping("/{subId}")
    public ResponseEntity<User> partialUpdateUser(
            @PathVariable String subId,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.partialUpdate(subId, request)
                .map(updatedUser -> {
                    enrichProfilePicUrl(updatedUser);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElseGet(() -> {
                    logger.warn("User not found for partial update: sub_id: {}", subId);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{subId}")
    public ResponseEntity<User> updateUser(
            @PathVariable String subId,
            @Validated({OnUpdate.class, Default.class}) 
            @RequestBody User user
        ) {
        user.setSub_id(subId);

        return userService.updateUser(user)
                .map(updatedUser -> {
                    enrichProfilePicUrl(updatedUser);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElseGet(() -> {
                    logger.warn("User not found for update: sub_id: {}", subId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{subId}/nemesis/{nemesisSubId}")
    public ResponseEntity<Void> removeNemesis(
            @PathVariable String subId,
            @PathVariable String nemesisSubId) {
        if (userService.removeNemesis(subId, nemesisSubId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{subId}")
    public ResponseEntity<Void> deleteUser(
        @PathVariable String subId 
    ) {
        if (userService.deleteUserById(subId)) {
            return ResponseEntity.noContent().build();
        }
        logger.warn("User not found for deletion: sub_id: {}", subId);
       
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("me")
    public ResponseEntity<Void> deleteCurrentUser() {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (userService.deleteCurrentUser(subId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("me")
    public ResponseEntity<User> getCurrentUser() {
        String subId = SecurityUtils.getCurrentUserSub();
        if (subId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userService.getUserById(subId)
                .map(user -> {
                    enrichProfilePicUrl(user);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<User> getUser(
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String subId
        ) {
        if (subId != null && !subId.isBlank()) {
            return userService.getUserById(subId)
                    .map(user -> {
                        enrichProfilePicUrl(user);
                        return ResponseEntity.ok(user);
                    })
                    .orElseGet(() -> {
                        logger.warn("User not found with sub_id: {}", subId);
                        return ResponseEntity.notFound().build();
                    });

        }
        if (username != null && !username.isBlank()) {
            return userService.getUserByUsername(username)
                    .map(user -> {
                        enrichProfilePicUrl(user);
                        return ResponseEntity.ok(user);
                    })
                    .orElseGet(() -> {
                        logger.warn("User not found with username: {}", username);
                        return ResponseEntity.notFound().build();
                    });
        }
        logger.warn("Get user request missing both username and subId parameters");
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10) Pageable pageable) {
        if (q == null || q.isBlank() || q.trim().length() < 2) {
            Page<User> emptyPage = new PageImpl<>(
                    java.util.Collections.emptyList(),
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()),
                    0);
            return ResponseEntity.ok(emptyPage);
        }
        try {
            Page<User> results = userService.searchUsers(q.trim(), pageable);
            results.getContent().forEach(this::enrichProfilePicUrl);
            return ResponseEntity.ok(results);
        } catch (Exception ex) {
            logger.warn("[SEARCH] Failed for q={}: {}", q, ex.getMessage());
            Page<User> emptyPage = new PageImpl<>(
                    java.util.Collections.emptyList(),
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()),
                    0);
            return ResponseEntity.ok(emptyPage);
        }
    }

    /**
     * Replaces profilePicUrl (S3 object key) with a presigned download URL for display.
     * Only if profilePicUrl looks like an S3 key (not already a full URL).
     */
    private void enrichProfilePicUrl(User user) {
        if (user == null || user.getProfilePicUrl() == null || user.getProfilePicUrl().isBlank()) return;
        String key = user.getProfilePicUrl();
        if (key.startsWith("http://") || key.startsWith("https://")) return; // already a URL
        try {
            String presignedUrl = s3Service.generatePresignedDownloadUrl(key);
            user.setProfilePicUrl(presignedUrl);
        } catch (Exception e) {
            logger.warn("[PROFILE-PIC] Failed to generate presigned URL for key {}: {}", key, e.getMessage());
        }
    }
}
