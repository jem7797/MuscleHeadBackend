package com.MuscleHead.MuscleHead.LiveSession;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MuscleHead.MuscleHead.config.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/live-sessions")
public class LiveSessionController {

    private static final Logger logger = LoggerFactory.getLogger(LiveSessionController.class);

    @Autowired
    private LiveSessionService liveSessionService;

    @PostMapping("/create")
    public ResponseEntity<CreateSessionResponse> createSession() {
        String userId = SecurityUtils.getCurrentUserSub();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CreateSessionResponse response = liveSessionService.createSession(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{sessionId}/invite")
    public ResponseEntity<Void> sendInvite(@PathVariable UUID sessionId, @Valid @RequestBody InviteRequest request) {
        String fromUserId = SecurityUtils.getCurrentUserSub();
        if (fromUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        liveSessionService.sendInvite(sessionId, fromUserId, request.getToUserId(), request.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/invites/{inviteId}/accept")
    public ResponseEntity<Void> acceptInvite(@PathVariable UUID inviteId) {
        String userId = SecurityUtils.getCurrentUserSub();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        liveSessionService.acceptInvite(inviteId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invites/{inviteId}/decline")
    public ResponseEntity<Void> declineInvite(@PathVariable UUID inviteId) {
        String userId = SecurityUtils.getCurrentUserSub();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        liveSessionService.declineInvite(inviteId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<Void> endSession(@PathVariable UUID sessionId) {
        String userId = SecurityUtils.getCurrentUserSub();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        liveSessionService.endSession(sessionId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionDetailsResponse> getSession(@PathVariable UUID sessionId) {
        try {
            SessionDetailsResponse response = liveSessionService.getSession(sessionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{sessionId}/status")
    public ResponseEntity<Map<String, String>> getSessionStatus(@PathVariable UUID sessionId) {
        String userId = SecurityUtils.getCurrentUserSub();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String status = liveSessionService.getSessionStatusForUser(sessionId, userId);
        return ResponseEntity.ok(Map.of("status", status));
    }

    @GetMapping("/invites/pending")
    public ResponseEntity<List<PendingInviteResponse>> getPendingInvites() {
        String userId = SecurityUtils.getCurrentUserSub();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PendingInviteResponse> invites = liveSessionService.getPendingInvites(userId);
        return ResponseEntity.ok(invites);
    }

    @GetMapping("/invites/pending/unseen")
    public ResponseEntity<List<PendingInviteResponse>> getUnseenPendingInvites() {
        String userId = SecurityUtils.getCurrentUserSub();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PendingInviteResponse> invites = liveSessionService.getUnseenPendingInvites(userId);
        return ResponseEntity.ok(invites);
    }

    @PostMapping("/invites/{inviteId}/toast-seen")
    public ResponseEntity<Void> markInviteToastSeen(@PathVariable UUID inviteId) {
        String userId = SecurityUtils.getCurrentUserSub();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        liveSessionService.markInviteToastSeen(inviteId, userId);
        return ResponseEntity.noContent().build();
    }
}
