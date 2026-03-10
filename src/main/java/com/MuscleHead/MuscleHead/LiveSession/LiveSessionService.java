package com.MuscleHead.MuscleHead.LiveSession;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.exception.LiveSessionForbiddenException;

import jakarta.transaction.Transactional;

@Service
public class LiveSessionService {

    private static final Logger logger = LoggerFactory.getLogger(LiveSessionService.class);

    @Autowired
    private LiveWorkoutSessionRepository sessionRepository;

    @Autowired
    private SessionInviteRepository inviteRepository;

    @Autowired
    private LiveSessionExerciseRepository exerciseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CreateSessionResponse createSession(String hostUserId) {
        if (hostUserId == null || hostUserId.isBlank()) {
            throw new IllegalArgumentException("Host user ID is required");
        }
        userRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + hostUserId));

        LiveWorkoutSession session = new LiveWorkoutSession();
        session.setHostUserId(hostUserId);
        session.setStatus(LiveWorkoutSession.SessionStatus.PENDING);
        session = sessionRepository.save(session);

        logger.info("Created live workout session {} for host {}", session.getId(), hostUserId);

        return new CreateSessionResponse(
                session.getId(),
                session.getHostUserId(),
                session.getStatus().name(),
                session.getCreatedAt());
    }

    @Transactional
    public void sendInvite(UUID sessionId, String fromUserId, String toUserId, String message) {
        if (sessionId == null || fromUserId == null || fromUserId.isBlank() || toUserId == null || toUserId.isBlank()) {
            throw new IllegalArgumentException("Session ID, from user ID, and to user ID are required");
        }

        LiveWorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getHostUserId().equals(fromUserId)) {
            throw new IllegalArgumentException("Only the host can send invites");
        }

        if (session.getStatus() != LiveWorkoutSession.SessionStatus.PENDING) {
            throw new IllegalStateException("Cannot send invites for a session that is not pending");
        }

        userRepository.findById(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + toUserId));

        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Cannot invite yourself");
        }

        SessionInvite invite = new SessionInvite();
        invite.setSession(session);
        invite.setFromUserId(fromUserId);
        invite.setToUserId(toUserId);
        invite.setMessage(message != null ? message : "");
        invite.setStatus(SessionInvite.InviteStatus.pending);
        inviteRepository.save(invite);

        logger.info("Sent invite {} for session {} from {} to {}", invite.getId(), sessionId, fromUserId, toUserId);
    }

    @Transactional
    public void acceptInvite(UUID inviteId, String userId) {
        if (inviteId == null || userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invite ID and user ID are required");
        }

        SessionInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found: " + inviteId));

        if (!invite.getToUserId().equals(userId)) {
            throw new IllegalArgumentException("Only the invited user can accept this invite");
        }

        if (invite.getStatus() != SessionInvite.InviteStatus.pending) {
            throw new IllegalStateException("Invite has already been " + invite.getStatus());
        }

        LiveWorkoutSession session = invite.getSession();
        if (session.getStatus() != LiveWorkoutSession.SessionStatus.PENDING) {
            throw new IllegalStateException("Session is no longer available");
        }

        invite.setStatus(SessionInvite.InviteStatus.accepted);
        inviteRepository.save(invite);

        session.setGuestUserId(userId);
        session.setStatus(LiveWorkoutSession.SessionStatus.in_progress);
        sessionRepository.save(session);

        logger.info("User {} accepted invite {} for session {}", userId, inviteId, session.getId());
    }

    @Transactional
    public void declineInvite(UUID inviteId, String userId) {
        if (inviteId == null || userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invite ID and user ID are required");
        }

        SessionInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found: " + inviteId));

        if (!invite.getToUserId().equals(userId)) {
            throw new IllegalArgumentException("Only the invited user can decline this invite");
        }

        if (invite.getStatus() != SessionInvite.InviteStatus.pending) {
            throw new IllegalStateException("Invite has already been " + invite.getStatus());
        }

        invite.setStatus(SessionInvite.InviteStatus.declined);
        inviteRepository.save(invite);

        logger.info("User {} declined invite {} for session {}", userId, inviteId, invite.getSession().getId());
    }

    @Transactional
    public void endSession(UUID sessionId, String userId) {
        if (sessionId == null || userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Session ID and user ID are required");
        }

        LiveWorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getHostUserId().equals(userId)) {
            throw new LiveSessionForbiddenException("Only the host can end the session");
        }

        if (session.getStatus() == LiveWorkoutSession.SessionStatus.ENDED) {
            throw new IllegalStateException("Session has already ended");
        }

        session.setStatus(LiveWorkoutSession.SessionStatus.ENDED);
        sessionRepository.save(session);

        logger.info("User {} ended session {}", userId, sessionId);
    }

    public SessionDetailsResponse getSession(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID is required");
        }

        LiveWorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        List<LiveSessionExercise> allExercises = exerciseRepository.findBySessionId(sessionId);
        String hostId = session.getHostUserId();
        String guestId = session.getGuestUserId();

        List<LiveSessionExerciseDto> hostExercises = allExercises.stream()
                .filter(e -> hostId != null && hostId.equals(e.getUserId()))
                .map(LiveSessionExerciseDto::from)
                .collect(Collectors.toList());

        List<LiveSessionExerciseDto> guestExercises = allExercises.stream()
                .filter(e -> guestId != null && guestId.equals(e.getUserId()))
                .map(LiveSessionExerciseDto::from)
                .collect(Collectors.toList());

        return new SessionDetailsResponse(
                session.getId(),
                session.getHostUserId(),
                session.getGuestUserId(),
                session.getStatus().name(),
                session.getCreatedAt(),
                hostExercises,
                guestExercises);
    }

    public List<PendingInviteResponse> getPendingInvites(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        List<SessionInvite> invites = inviteRepository.findPendingInvitesForUser(userId);
        return invites.stream()
                .map(i -> new PendingInviteResponse(
                        i.getId(),
                        i.getSession().getId(),
                        i.getFromUserId(),
                        i.getMessage() != null ? i.getMessage() : "",
                        i.getSentAt()))
                .collect(Collectors.toList());
    }
}
