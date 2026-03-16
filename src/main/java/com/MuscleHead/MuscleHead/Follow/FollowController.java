package com.MuscleHead.MuscleHead.Follow;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.MuscleHead.MuscleHead.config.SecurityUtils;

@RestController
@RequestMapping("follow/api/")
public class FollowController {

    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

    @Autowired
    private FollowService followService;

    @PostMapping("follow/{followeeSubId}")
    public ResponseEntity<Void> follow(@PathVariable String followeeSubId) {
        String followerSubId = SecurityUtils.getCurrentUserSub();
        if (followerSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            followService.follow(followerSubId, followeeSubId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("request/{followeeSubId}")
    public ResponseEntity<Void> createFollowRequest(@PathVariable String followeeSubId) {
        String requesterSubId = SecurityUtils.getCurrentUserSub();
        if (requesterSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            followService.createFollowRequest(requesterSubId, followeeSubId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("requests")
    public ResponseEntity<List<FollowRequestResponse>> getPendingRequests() {
        String followeeSubId = SecurityUtils.getCurrentUserSub();
        if (followeeSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(followService.getPendingRequests(followeeSubId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("requests/{id}/accept")
    public ResponseEntity<Void> acceptRequest(@PathVariable UUID id) {
        String followeeSubId = SecurityUtils.getCurrentUserSub();
        if (followeeSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            followService.acceptRequest(id, followeeSubId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("requests/{id}/decline")
    public ResponseEntity<Void> declineRequest(@PathVariable UUID id) {
        String followeeSubId = SecurityUtils.getCurrentUserSub();
        if (followeeSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            followService.declineRequest(id, followeeSubId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("request-status")
    public ResponseEntity<java.util.Map<String,String>> getRequestStatus(
            @RequestParam String requester,
            @RequestParam String followee) {
        String status = followService.getRequestStatus(requester, followee);
        return ResponseEntity.ok(java.util.Map.of("status", status));
    }

    @DeleteMapping("unfollow/{followeeSubId}")
    public ResponseEntity<Void> unfollow(@PathVariable String followeeSubId) {
        String followerSubId = SecurityUtils.getCurrentUserSub();
        if (followerSubId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            followService.unfollow(followerSubId, followeeSubId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("followers/{subId}")
    public ResponseEntity<List<UserSummary>> getFollowers(@PathVariable String subId) {
        try {
            return ResponseEntity.ok(followService.getFollowers(subId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("following/{subId}")
    public ResponseEntity<List<UserSummary>> getFollowing(@PathVariable String subId) {
        try {
            return ResponseEntity.ok(followService.getFollowing(subId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("check")
    public ResponseEntity<Boolean> isFollowing(
            @RequestParam String follower,
            @RequestParam String followee) {
        return ResponseEntity.ok(followService.isFollowing(follower, followee));
    }

    @GetMapping("mutual")
    public ResponseEntity<Boolean> areMutualFollowers(
            @RequestParam String user1,
            @RequestParam String user2) {
        return ResponseEntity.ok(followService.areMutualFollowers(user1, user2));
    }
}
