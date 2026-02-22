package com.MuscleHead.MuscleHead.Follow;

import java.util.List;

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
}
