package com.MuscleHead.MuscleHead.LiveSession;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;
import com.MuscleHead.MuscleHead.exception.LiveSessionForbiddenException;

import jakarta.transaction.Transactional;

@Service
public class LiveSessionService {
    private static final java.time.Duration INVITE_VISIBILITY_WINDOW = java.time.Duration.ofHours(24);
    private static final int INVITE_DELETE_BATCH_SIZE = 2_000;

    @Autowired
    private LiveWorkoutSessionRepository sessionRepository;

    @Autowired
    private SessionInviteRepository inviteRepository;

    @Autowired
    private LiveSessionExerciseRepository exerciseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Clock utcClock;

    private static Instant inviteVisibilityCutoff() {
        return Instant.now().minus(INVITE_VISIBILITY_WINDOW);
    }

    @Transactional
    public CreateSessionResponse createSession(String hostUserId) {
        if (hostUserId == null || hostUserId.isBlank()) {
            throw new IllegalArgumentException("Host user ID is required");
        }
      
        User host = userRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + hostUserId));

        LiveWorkoutSession session = new LiveWorkoutSession();
        session.setHostUserId(hostUserId);
        session.setStatus(LiveWorkoutSession.SessionStatus.PENDING);
        session.setHostUserName(host.getUsername());
        session = sessionRepository.save(session);

        return new CreateSessionResponse(
                session.getId(),
                session.getHostUserId(),
                session.getStatus().name(),
                session.getCreatedAt(),
                session.getHostUserName()
        );
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

        User guest = userRepository.findById(toUserId)
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
        invite.setHostUserName(session.getHostUserName());
        invite.setGuestUserName(guest.getUsername() != null ? guest.getUsername() : "");
        inviteRepository.save(invite);

        session.setGuestUserId(toUserId);
        session.setGuestUserName(guest.getUsername());
        sessionRepository.save(session);
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
        startTimerInternal(session, utcNow());
        sessionRepository.save(session);
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

        stopTimerInternal(session, utcNow());
        session.setStatus(LiveWorkoutSession.SessionStatus.ENDED);
        sessionRepository.save(session);
    }

    public LiveSessionTimerResponse getTimer(UUID sessionId, String userId) {
        LiveWorkoutSession session = requireParticipant(sessionId, userId);
        return toTimerResponse(session, utcNow());
    }

    @Transactional
    public LiveSessionTimerResponse startTimer(UUID sessionId, String userId) {
        LiveWorkoutSession session = requireHost(sessionId, userId);
        requireSessionInProgress(session);
        if (session.getTimerState() == TimerState.RUNNING) {
            return toTimerResponse(session, utcNow());
        }
        if (session.getTimerState() == TimerState.PAUSED) {
            throw new IllegalStateException("Timer is paused; use resume instead");
        }
        Instant now = utcNow();
        session.setTimerElapsedSeconds(0);
        startTimerInternal(session, now);
        sessionRepository.save(session);
        return toTimerResponse(session, now);
    }

    @Transactional
    public LiveSessionTimerResponse pauseTimer(UUID sessionId, String userId) {
        LiveWorkoutSession session = requireHost(sessionId, userId);
        requireSessionInProgress(session);
        if (session.getTimerState() != TimerState.RUNNING) {
            throw new IllegalStateException("Timer is not running");
        }
        Instant now = utcNow();
        pauseTimerInternal(session, now);
        sessionRepository.save(session);
        return toTimerResponse(session, now);
    }

    @Transactional
    public LiveSessionTimerResponse resumeTimer(UUID sessionId, String userId) {
        LiveWorkoutSession session = requireHost(sessionId, userId);
        requireSessionInProgress(session);
        if (session.getTimerState() != TimerState.PAUSED) {
            throw new IllegalStateException("Timer is not paused");
        }
        Instant now = utcNow();
        session.setTimerStartedAt(now);
        session.setTimerState(TimerState.RUNNING);
        sessionRepository.save(session);
        return toTimerResponse(session, now);
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

        Instant now = utcNow();
        return new SessionDetailsResponse(
                session.getId(),
                session.getHostUserId(),
                session.getGuestUserId(),
                session.getStatus().name(),
                session.getCreatedAt(),
                session.getHostUserName(),
                session.getGuestUserName(),
                hostExercises,
                guestExercises,
                toTimerResponse(session, now));
    }

    public String getSessionStatusForUser(UUID sessionId, String userId) {
        if (sessionId == null || userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Session ID and user ID are required");
        }

        LiveWorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        boolean isHost = userId.equals(session.getHostUserId());
        boolean isGuest = userId.equals(session.getGuestUserId());
        if (!isHost && !isGuest) {
            throw new LiveSessionForbiddenException("You are not a participant of this session");
        }

        return session.getStatus().name().toLowerCase();
    }

    public List<PendingInviteResponse> getPendingInvites(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        Instant cutoff = inviteVisibilityCutoff();
        List<SessionInvite> invites = inviteRepository.findPendingInvitesForUser(userId, cutoff);
        return invites.stream()
                .map(this::toPendingInviteResponse)
                .collect(Collectors.toList());
    }

    public List<PendingInviteResponse> getUnseenPendingInvites(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        Instant cutoff = inviteVisibilityCutoff();
        List<SessionInvite> invites = inviteRepository.findUnseenPendingInvitesForUser(userId, cutoff);
        return invites.stream()
                .map(this::toPendingInviteResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markInviteToastSeen(UUID inviteId, String userId) {
        if (inviteId == null || userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invite ID and user ID are required");
        }

        int updated = inviteRepository.markRecipientToastSeenIfNull(inviteId, userId, Instant.now());
        if (updated == 0 && !inviteRepository.existsByIdAndToUserId(inviteId, userId)) {
            throw new IllegalArgumentException("Invite not found for user: " + inviteId);
        }
    }

    public long deleteExpiredInvites() {
        Instant cutoff = inviteVisibilityCutoff();
        long total = 0;
        int deleted;
        do {
            deleted = inviteRepository.deleteExpiredInvitesBatch(cutoff, INVITE_DELETE_BATCH_SIZE);
            total += deleted;
        } while (deleted > 0);
        return total;
    }

    private PendingInviteResponse toPendingInviteResponse(SessionInvite i) {
        return new PendingInviteResponse(
                i.getId(),
                i.getSession().getId(),
                i.getFromUserId(),
                i.getMessage() != null ? i.getMessage() : "",
                i.getSentAt(),
                i.getHostUserName(),
                i.getGuestUserName() != null ? i.getGuestUserName() : "",
                i.getStatus() != null ? i.getStatus().name() : null);
    }

    private LiveWorkoutSession requireParticipant(UUID sessionId, String userId) {
        LiveWorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        boolean isHost = userId.equals(session.getHostUserId());
        boolean isGuest = userId.equals(session.getGuestUserId());
        if (!isHost && !isGuest) {
            throw new LiveSessionForbiddenException("You are not a participant of this session");
        }
        return session;
    }

    private LiveWorkoutSession requireHost(UUID sessionId, String userId) {
        LiveWorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        if (!session.getHostUserId().equals(userId)) {
            throw new LiveSessionForbiddenException("Only the host can control the session timer");
        }
        return session;
    }

    private static void requireSessionInProgress(LiveWorkoutSession session) {
        if (session.getStatus() != LiveWorkoutSession.SessionStatus.in_progress) {
            throw new IllegalStateException("Timer can only be controlled while the session is in progress");
        }
    }

    static long computeElapsedSeconds(LiveWorkoutSession session, Instant now) {
        long elapsed = session.getTimerElapsedSeconds();
        if (session.getTimerState() == TimerState.RUNNING && session.getTimerStartedAt() != null) {
            elapsed += Duration.between(session.getTimerStartedAt(), now).getSeconds();
        }
        return Math.max(0, elapsed);
    }

    private static void startTimerInternal(LiveWorkoutSession session, Instant now) {
        session.setTimerStartedAt(now);
        session.setTimerState(TimerState.RUNNING);
    }

    private static void pauseTimerInternal(LiveWorkoutSession session, Instant now) {
        if (session.getTimerStartedAt() != null) {
            long segmentSeconds = Duration.between(session.getTimerStartedAt(), now).getSeconds();
            session.setTimerElapsedSeconds(session.getTimerElapsedSeconds() + segmentSeconds);
        }
        session.setTimerStartedAt(null);
        session.setTimerState(TimerState.PAUSED);
    }

    private static void stopTimerInternal(LiveWorkoutSession session, Instant now) {
        if (session.getTimerState() == TimerState.RUNNING) {
            pauseTimerInternal(session, now);
        }
        session.setTimerState(TimerState.STOPPED);
    }

    private LiveSessionTimerResponse toTimerResponse(LiveWorkoutSession session, Instant now) {
        return new LiveSessionTimerResponse(
                computeElapsedSeconds(session, now),
                session.getTimerState(),
                now);
    }

    private Instant utcNow() {
        return Instant.now(utcClock);
    }
}
